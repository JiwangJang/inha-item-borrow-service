import { AxiosError } from "axios";

export default function errorHandler(error: AxiosError) {
    console.error(error);

    const errorObj = error.response?.data as { data: { errorCode: string; errorMessage: string }; success: boolean };
    const errorMessage = errorObj?.data.errorMessage;
    if (error.status == 500) {
        alert("서버 내부에러입니다. 잠시후 다시 시도해주시고, 지속되면 관리자에게 연락하세요.");
    } else {
        alert(errorMessage);
    }
    console.log(error.response?.data);
}
