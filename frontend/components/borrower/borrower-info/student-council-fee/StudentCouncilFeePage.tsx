"use client";

import BorrowerContext from "@/context/BorrowerContext";
import { useContext, useState } from "react";
import FeeVerificationRequest from "./FeeVerificationRequest";
import FeeVerificationStatus from "./FeeVerificationStatus";
import StudentCouncilFeeVerificationInterface from "@/types/StudentCouncilFeeVerificationInterface";
import Button from "@/components/utilities/Button";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import axios, { AxiosError } from "axios";
import API_SERVER from "@/apiServer";
import errorHandler from "@/utilities/errorHandler";
import { useRouter } from "next/navigation";
import Image from "next/image";

export default function StudentCouncilFeePage({
    verification,
}: {
    verification: StudentCouncilFeeVerificationInterface | null;
}) {
    const router = useRouter();
    const [verifi, setVerifi] = useState<StudentCouncilFeeVerificationInterface | null>(verification);
    const borrowerContext = useContext(BorrowerContext);
    const borrowerInfo = borrowerContext.borrowerInfo;
    const [confirmModal, setConfirmModal] = useState(false);

    if (verification == null && borrowerInfo?.agreementVersion == null) {
        return (
            <div className="mt-5">
                <div>
                    <p className="black-20px mb-1">✅ 학생회비 납부여부 확인</p>
                    <p>개인정보 처리방침에 동의하셔야 학생회비 납부인증 신청이 가능합니다.</p>
                </div>
                <div className="w-full relative h-90 my-4 border border-boxBorder rounded-xl overflow-hidden">
                    <Image src={"/images/need-agreement.png"} fill objectFit="cover" alt="개인정보 동의사진" />
                </div>
                <Button
                    className="w-full py-3 bold-18px"
                    title="개인정보 수집동의 하러가기"
                    onClick={() => router.push("/borrower-info/agreement/v1")}
                />
            </div>
        );
    }

    const cancelFunc = async () => {
        try {
            await axios.delete(`${API_SERVER}/student-council-fee-verification`, { withCredentials: true });
            setVerifi({
                ...verifi!,
                verify: false,
                s3Link: null,
                requestAt: null,
                responseAt: null,
                denyReason: null,
            });
        } catch (error) {
            if (error instanceof AxiosError) {
                errorHandler(error);
            }
            console.log(error);
        }
    };

    return (
        <div className="mt-5">
            <p className="black-20px mb-1">✅ 학생회비 납부여부 확인</p>
            {verifi?.s3Link == null ? (
                <FeeVerificationRequest name={borrowerInfo?.name} />
            ) : (
                <FeeVerificationStatus verification={verifi} name={borrowerInfo?.name} />
            )}
            {verifi?.verify ? null : (
                <Button
                    title="제출 취소하기"
                    className="w-full py-3 mt-2 bg-red-400!"
                    onClick={() => {
                        if (verifi?.requestAt == null) return;
                        setConfirmModal(true);
                    }}
                />
            )}

            <ConfirmModal
                message="취소하시면 다시 신청하셔야 합니다. 그래도 하시겠습니까?"
                onClose={() => setConfirmModal(false)}
                onConfirm={cancelFunc}
                open={confirmModal}
                title="경고"
            />

            {verifi?.s3Link == null ? (
                <div className="mt-5">
                    <p className="bold-18px">[참고] 학생회비 납부인증 사진 예시 </p>
                    <p>사진클릭시 새창보기로 열립니다.</p>
                    <div
                        className="relative w-full h-75 mt-3 border-2 border-black"
                        onClick={() => window.open("/images/council-fee-example.png")}
                    >
                        <Image src={"/images/council-fee-example.png"} fill alt="학생회비 납부인증 사진 예시"></Image>
                    </div>
                </div>
            ) : null}
        </div>
    );
}
