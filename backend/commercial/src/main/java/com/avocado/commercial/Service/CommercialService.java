package com.avocado.commercial.Service;

import com.avocado.commercial.Dto.request.CommercialCancelReqDto;
import com.avocado.commercial.Dto.request.CommercialReqDto;
import com.avocado.commercial.Dto.response.Analysis;
import com.avocado.commercial.Dto.response.item.*;
import com.avocado.commercial.Dto.response.CommercialRespDto;
import com.avocado.commercial.Entity.Commercial;
import com.avocado.commercial.Entity.CommercialExposure;
import com.avocado.commercial.Entity.CommercialStatistic;
import com.avocado.commercial.Repository.CommercialExposureRepository;
import com.avocado.commercial.Repository.CommercialRepository;
import com.avocado.commercial.Repository.CommercialStatisticRepository;
import com.avocado.commercial.error.CommercialException;
import com.avocado.commercial.error.ErrorCode;
import com.avocado.commercial.util.JwtUtil;
import com.avocado.commercial.util.UUIDUtil;
import io.jsonwebtoken.Jwt;
import net.bytebuddy.agent.builder.AgentBuilder;
import com.avocado.commercial.kafka.KafkaProducer;
import com.avocado.commercial.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.net.http.HttpRequest;
import java.util.*;

@Service
// 리팩토링 무조건 필요할 듯
public class CommercialService {

    private CommercialRepository commercialRepository;
    private CommercialExposureRepository commercialExposureRepository;
    private CommercialStatisticRepository commercialStatisticRepository;
    private ImageService imageService;
    private JwtUtil jwtUtil;
    private final KafkaProducer kafkaproducer;
    private final int TYPE_POPUP = 0;
    private final int TYPE_CAROUSEL = 1;

    @Autowired
    public CommercialService(CommercialRepository commercialRepository, CommercialExposureRepository commercialExposureRepository,
                             ImageService imageService, JwtUtil jwtUtil, KafkaProducer kafkaproducer, CommercialStatisticRepository commercialStatisticRepository){
        this.commercialRepository = commercialRepository;
        this.commercialExposureRepository = commercialExposureRepository;
        this.commercialStatisticRepository = commercialStatisticRepository;
        this.imageService = imageService;
        this.jwtUtil = jwtUtil;
        this.kafkaproducer = kafkaproducer;
    }

    // 리팩토링 해야할 듯
    public CommercialRespDto getCommercialExposure(int mbtiId, int age, int personalColorId, char gender){
        checkCommercialRequest(age, gender, mbtiId, personalColorId);

        // 응답으로 넘길 광고 리스트
        List<Commercial> popupEntityList = null;
        List<Commercial> carouselEntityList = null;

        // 넘어온 상태 값에 따라 다른 리스트를 가져온다.
        if(gender == 'X'||age == -1){
            popupEntityList = commercialRepository.findByCommercialTypeId(TYPE_POPUP);
            carouselEntityList = commercialRepository.findByCommercialTypeId(TYPE_CAROUSEL);
        }else {
            // 나이 재설정
            // 나이는 10대씩 나누어 준다(70대 이상은 70대로 한다)
            age = age >= 70 ? 70 : age / 10 * 10;
            if (mbtiId == -1) {
                if (personalColorId == -1) {
                    popupEntityList = commercialRepository.findByAgeAndCommercialTypeIdAndGender(age, TYPE_POPUP, gender);
                    carouselEntityList = commercialRepository.findByAgeAndCommercialTypeIdAndGender(age, TYPE_CAROUSEL, gender);
                } else {
                    popupEntityList = commercialRepository.findByMbtiIdAndAgeAndCommercialTypeIdAndGender(mbtiId, age, TYPE_POPUP, gender);
                    carouselEntityList = commercialRepository.findByMbtiIdAndAgeAndCommercialTypeIdAndGender(mbtiId, age, TYPE_CAROUSEL, gender);
                }
            } else if (mbtiId == -1) {
                popupEntityList = commercialRepository.findByAgeAndCommercialTypeIdAndPersonalColorIdAndGender(age, TYPE_POPUP, personalColorId, gender);
                carouselEntityList = commercialRepository.findByAgeAndCommercialTypeIdAndPersonalColorIdAndGender(age, TYPE_CAROUSEL, personalColorId, gender);
            } else {
                popupEntityList = commercialRepository.findByMbtiIdAndAgeAndCommercialTypeIdAndPersonalColorIdAndGender(mbtiId, age, TYPE_POPUP, personalColorId, gender);
                carouselEntityList = commercialRepository.findByMbtiIdAndAgeAndCommercialTypeIdAndPersonalColorIdAndGender(mbtiId, age, TYPE_CAROUSEL, personalColorId, gender);
            }
        }

        // 응답으로 넘길 Dto
        CommercialRespDto commercialRespDto = new CommercialRespDto();
        List<Carousel> carouselList = new ArrayList<>();

        // 팝업을 사용하게 되면 스트림도 넘겨야한다.
        commercialRespDto.setPopup(popupEntityList.size() == 0 ? null : popupEntityList.get(0).toPopup());

        for(Commercial commercial : carouselEntityList){
            CommercialExposure commercialExposure = new CommercialExposure();
            carouselList.add(commercial.toCarousel());
            commercialExposure.setCommercialId(commercial.getId());
            commercialExposureRepository.save(commercialExposure);
            // produce to kafka
            kafkaproducer.sendAdview(commercial.getMerchandiseId(), UUID.randomUUID());
        }
        commercialRespDto.setCarousel_list(carouselList);

        return commercialRespDto;
    }


    public List<Analysis> getAnalyses(int commercialId){
        List<Analysis> analysisList = new ArrayList<>();

        Commercial commercial = commercialRepository.findById(commercialId);

        if(commercial == null){
            throw new CommercialException(ErrorCode.COMMERCIAL_NOT_FOUND);
        }

        List<Exposure> exposureList = commercialRepository.countExposureByCommercialId(commercialId);
        List<Click> clickList = commercialRepository.countClickByMerchandiseId(commercial.getMerchandiseId());
        List<Purchase> purchaseList = commercialRepository.countPurchaseByMerchandiseId(commercial.getMerchandiseId());

        Map<String,Analysis> analysisMap = new TreeMap<>();

        for(Exposure e : exposureList){
            Analysis analysis = new Analysis();
            analysis.setDate(e.getDate());
            analysis.setExposure_cnt(e.getExposure_Cnt());
            analysisMap.put(e.getDate(),analysis);
        }

        for(Click c : clickList){
            Analysis analysis;
            if(analysisMap.containsKey(c.getDate())){
                analysis = analysisMap.get(c.getDate());
                analysis.setClick_cnt(c.getClick_Cnt());
            }
            else{
                analysis = new Analysis();
                analysis.setClick_cnt(c.getClick_Cnt());
                analysis.setDate(c.getDate());
                analysisMap.put(c.getDate(),analysis);
            }

        }

        for(Purchase p : purchaseList){
            Analysis analysis;
            if(analysisMap.containsKey(p.getDate())){
                analysis = analysisMap.get(p.getDate());
                analysis.setPurchase_amount(p.getPurchase_Amount());
                analysis.setQuantity(p.getQuantity());
            }else{
                analysis = new Analysis();
                analysis.setDate(p.getDate());
                analysis.setQuantity(p.getQuantity());
                analysis.setPurchase_amount(p.getPurchase_Amount());
                analysisMap.put(p.getDate(),analysis);
            }

        }

        for(String key : analysisMap.keySet()){
            System.out.println(analysisMap.get(key));
            analysisList.add(analysisMap.get(key));
        }

        return analysisList;
    }
    

    public void saveCommercial(CommercialReqDto commercialReqDto, HttpServletRequest request){
        checkCommercialRequest(commercialReqDto);
        String imgurl = imageService.createCommercialImages(commercialReqDto.getFile());
        if(imgurl == null){
            throw new CommercialException(ErrorCode.BAD_FILE_FORMAT);
        }
        UUID uuid = jwtUtil.getId(request);
        Commercial commercial = commercialReqDto.toEntity();
        commercial.setProviderId(uuid);
        commercial.setImgurl(imgurl);

        commercialRepository.save(commercial);
    }

    public List<Commercial> getRegistedCommercial(HttpServletRequest request){
        UUID uuid = jwtUtil.getId(request);

        List<Commercial> commercialList = commercialRepository.findByProviderId(uuid);

        return commercialList;
    }

    @Transactional
    public void removeCommercial(CommercialCancelReqDto commercialCancelReqDto, HttpServletRequest request) {
        UUID uuid = jwtUtil.getId(request);
        System.out.println(commercialCancelReqDto);
        try {
            commercialExposureRepository.deleteByCommercialId(commercialCancelReqDto.getCommercial_id());
            commercialRepository.deleteByIdAndProviderId(commercialCancelReqDto.getCommercial_id(),uuid);
        }catch (EmptyResultDataAccessException e){
            e.printStackTrace();
            throw new CommercialException(ErrorCode.COMMERCIAL_NOT_FOUND);
        }


    }


    public List<CommercialStatistic> getCommercialStatistic(int commercialId){
        List<CommercialStatistic> commercialStatisticList = commercialStatisticRepository.findByCommercialId(commercialId);
        return commercialStatisticList;
    }


    public void checkCommercialRequest(CommercialReqDto commercialReqDto){
        if(commercialReqDto.getAge() < 0){
            throw new CommercialException(ErrorCode.BAD_AGE);
        }
        if(commercialReqDto.getGender() != 'M' && commercialReqDto.getGender() != 'F' ){
            throw new CommercialException(ErrorCode.BAD_GENDER_TYPE);
        }
        if(commercialReqDto.getMbti_id() < 0 || 15 < commercialReqDto.getMbti_id()){
            throw new CommercialException(ErrorCode.BAD_MBTI_ID);
        }
        if(commercialReqDto.getCommercial_type_id() < 0 || 1 < commercialReqDto.getCommercial_type_id()){
            throw new CommercialException(ErrorCode.BAD_COMMERCIAL_TYPE);
        }
        if(commercialReqDto.getPersonal_color_id() < 0 || 9 < commercialReqDto.getPersonal_color_id()){
            throw new CommercialException(ErrorCode.BAD_PERSONAL_COLOR);
        }
    }
    public void checkCommercialRequest(int age, char gender, int mbtiId, int personalColorId){
        if(age < -1){
            throw new CommercialException(ErrorCode.BAD_AGE);
        }
        if(gender != 'M' && gender != 'F' && gender != 'X'){
            throw new CommercialException(ErrorCode.BAD_GENDER_TYPE);
        }
        if(mbtiId < -1 || 15 < mbtiId){
            throw new CommercialException(ErrorCode.BAD_MBTI_ID);
        }
        if(personalColorId < -1 || 9 < personalColorId){
            throw new CommercialException(ErrorCode.BAD_PERSONAL_COLOR);
        }
    }
}