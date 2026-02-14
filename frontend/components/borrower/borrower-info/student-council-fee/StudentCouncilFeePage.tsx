"use client";

import BorrowerContext from "@/context/BorrowerContext";
import { useContext } from "react";
import FeeVerificationRequest from "./FeeVerificationRequest";
import FeeVerificationStatus from "./FeeVerificationStatus";
import StudentCouncilFeeVerificationInterface from "@/types/StudentCouncilFeeVerificationInterface";

export default function StudentCouncilFeePage({
    verification,
}: {
    verification: StudentCouncilFeeVerificationInterface | null;
}) {
    const borrowerContext = useContext(BorrowerContext);
    const borrowerInfo = borrowerContext.borrowerInfo;

    return (
        <div className="mt-5">
            <p className="black-20px mb-1">✅ 학생회비 납부여부 확인</p>
            {verification?.requestAt == null ? (
                <FeeVerificationRequest name={borrowerInfo?.name} />
            ) : (
                <FeeVerificationStatus verification={verification} name={borrowerInfo?.name} />
            )}
        </div>
    );
}
