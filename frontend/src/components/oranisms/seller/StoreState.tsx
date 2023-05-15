import styled from "@emotion/styled";
import { Stack, Button } from "@mui/material";
import { ChartGender, ChartMbti, ChartPersonalColor } from "../charts";
import MouseOutlinedIcon from "@mui/icons-material/MouseOutlined";
import LocalAtmOutlinedIcon from "@mui/icons-material/LocalAtmOutlined";
import CheckroomOutlinedIcon from "@mui/icons-material/CheckroomOutlined";
import LocalShippingOutlinedIcon from "@mui/icons-material/LocalShippingOutlined";
import { useDispatch, useSelector } from "react-redux";
import { Member, clearAuth, setMember } from "@/src/features/auth/authSlice";
import router from "next/router";
import {
  removeToken,
  removeTokenAll,
  setToken,
} from "@/src/utils/tokenManager";
import { InlineText, BlockText } from "../../atoms";
const StoreState = () => {
  const dispatch = useDispatch();

  const handleLogout = () => {
    console.log("로그아웃 버튼이 클릭되었습니다.");
    removeTokenAll();
    dispatch(clearAuth());
    //로그인 페이지로 이동
    router.push("/login");
  };

  return (
    <Stack direction="column" spacing={1}>
      <Stack direction="row" justifyContent={"space-between"} spacing={1}>
        <IconBox>
          <MouseOutlinedIcon />
          <InlineText type="L" size="0.8rem">
            조회 수
          </InlineText>
          <InlineText>12,604</InlineText>
        </IconBox>
        <IconBox>
          <LocalShippingOutlinedIcon />
          <InlineText type="L" size="0.8rem">
            판매 수
          </InlineText>
          <InlineText>6,040</InlineText>
        </IconBox>
      </Stack>
      <Stack direction="row" justifyContent={"space-between"} spacing={1}>
        <IconBox>
          <LocalAtmOutlinedIcon />
          <InlineText type="L" size="0.8rem">
            총 판매 액
          </InlineText>
          <InlineText>3,000,000원</InlineText>
        </IconBox>
        <IconBox>
          <CheckroomOutlinedIcon />
          <InlineText type="L" size="0.8rem">
            상품 수
          </InlineText>
          <InlineText>604</InlineText>
        </IconBox>
      </Stack>
      <Stack spacing={2}>
        <ChartGender />
        <ChartMbti />
        <ChartPersonalColor />
        {/* 로그아웃 버튼 */}
        <Button
          fullWidth
          color="error"
          variant="outlined"
          style={{ padding: "20px" }}
          onClick={handleLogout}
        >
          로그아웃
        </Button>
      </Stack>
    </Stack>
  );
};

export default StoreState;

const IconBox = styled.div`
  border: 1px solid #dddddd;
  border-radius: 10px;
  height: 80px;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
`;