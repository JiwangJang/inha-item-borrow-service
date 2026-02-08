"use client";

import BorrowerContext from "@/context/BorrowerContext";
import { useRouter } from "next/navigation";
import { useContext } from "react";

export default function Page() {
    const router = useRouter();
    const borrowerContext = useContext(BorrowerContext);

    if (borrowerContext.borrowerInfo == null) {
        alert("로그인부터 진행해주시기 바랍니다.");
        router.push("/borrower-info/login");
    }
    // 여기 서버컴포넌트로 바꾸기
    return <div>대여자 정보확인 페이지</div>;
}
