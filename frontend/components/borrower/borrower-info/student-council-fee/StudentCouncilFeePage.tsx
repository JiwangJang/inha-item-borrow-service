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

export default function StudentCouncilFeePage({
    verification,
}: {
    verification: StudentCouncilFeeVerificationInterface;
}) {
    const [verifi, setVerifi] = useState<StudentCouncilFeeVerificationInterface>(verification);
    const borrowerContext = useContext(BorrowerContext);
    const borrowerInfo = borrowerContext.borrowerInfo;
    const [confirmModal, setConfirmModal] = useState(false);

    const cancelFunc = async () => {
        try {
            await axios.delete(`${API_SERVER}/student-council-fee-verification`, { withCredentials: true });
            setVerifi({
                ...verifi,
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
            {verifi?.requestAt == null ? (
                <FeeVerificationRequest name={borrowerInfo?.name} />
            ) : (
                <FeeVerificationStatus verification={verifi} name={borrowerInfo?.name} />
            )}
            <Button
                title="제출 취소하기"
                className="w-full py-3 mt-2 bg-placeholder!"
                onClick={() => {
                    if (verifi?.requestAt == null) return;
                    setConfirmModal(true);
                }}
            />
            <ConfirmModal
                message="취소하시면 더이상 물품대여가 불가능합니다. 그래도 하시겠습니까?"
                onClose={() => setConfirmModal(false)}
                onConfirm={cancelFunc}
                open={confirmModal}
                title="경고"
            />
        </div>
    );
}
