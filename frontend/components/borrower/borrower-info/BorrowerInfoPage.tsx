"use client";

import API_SERVER from "@/apiServer";
import BorrowerContext from "@/context/BorrowerContext";
import AGREEMENT_AGREEMENT_VERSION from "@/utilities/agreementVersion";
import axios from "axios";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useContext } from "react";

export default function BorrowerInfoPage() {
    const router = useRouter();
    const borrowerContext = useContext(BorrowerContext);
    const borrowerInfo = borrowerContext.borrowerInfo;
    const className = "px-5 py-4 border-b border-boxBorder last:border-0";

    return (
        <div className="mt-5">
            <p className="black-20px">😃 안녕하세요! {borrowerInfo?.name}님!</p>
            <div className="w-full border border-boxBorder flex flex-col bg-white rounded mt-2">
                <Link href={"/borrower-info/me"} className={className}>
                    내 정보 확인
                </Link>
                <Link href={`/borrower-info/agreement/${AGREEMENT_AGREEMENT_VERSION}`} className={className}>
                    개인정보 처리방침 {borrowerInfo?.accountNumber == null ? "동의하기" : "확인하기"}
                </Link>
                <Link href={"/borrower-info/student-council-fee"} className={className}>
                    등록금 납부 인증
                </Link>
                <div
                    className={className + " cursor-pointer"}
                    onClick={async () => {
                        if (borrowerContext.setBorrowerInfo) {
                            await axios.get(`${API_SERVER}/logout`, { withCredentials: true });
                            alert("로그아웃이 완료됐습니다.");
                            borrowerContext.setBorrowerInfo(null);
                            router.push("/");
                        }
                    }}
                >
                    로그아웃
                </div>
            </div>
        </div>
    );
}
