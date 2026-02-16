import { AxiosError } from "axios";

export default function errorHandler(error: AxiosError) {
    console.error(error);
    if (error.status == 500) {
        alert("서버 내부에러입니다. 잠시후 다시 시도해주시고, 지속되면 관리자에게 연락하세요.");
    } else if (error.status == 400) {
        alert("잘못된 입력값을 작성하셨습니다.");
    } else {
        alert("알 수 없는 에러입니다. 새로고침후 다시 시도해보시고, 지속되면 관리자에게 연락해주세요.");
    }
    console.log(error.response?.data);
}
