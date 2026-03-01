"use client";

import { useRouter } from "next/navigation";
import Button from "../utilities/Button";
import ItemSection from "./borrower-home-page/ItemSection";
import NoticeSection from "./borrower-home-page/NoticeSection";
import { useContext, useEffect, useState } from "react";
import BorrowerContext from "@/context/BorrowerContext";
import ConfirmModal from "../utilities/modal/ConfirmModal";
import AlertModal from "../utilities/modal/AlertModal";
import AGREEMENT_AGREEMENT_VERSION from "@/utilities/agreementVersion";

export default function BorrowerHomePage() {
    const { borrowerInfo } = useContext(BorrowerContext);
    const [confirmModal, setConfirmModal] = useState(false);
    const [alertModal, setAlertModal] = useState(false);
    const [banReason, setBanReason] = useState("");
    const router = useRouter();

    useEffect(() => {
        if (borrowerInfo?.id != null && borrowerInfo?.agreementVersion == null) {
            setConfirmModal(true);
        }

        if (borrowerInfo?.ban && borrowerInfo?.banReason != null) {
            setAlertModal(true);
            setBanReason(borrowerInfo.banReason);
        }
    }, []);
    return (
        <div className="mt-5 relative">
            <NoticeSection />
            <ItemSection />

            <div className="fixed w-full max-w-125 bottom-17.5 left-1/2 translate-x-[-50%] pl-6 pr-9">
                <Button
                    title={borrowerInfo?.id == null ? "로그인하기" : "대여신청"}
                    className="w-full py-3 bold-18px"
                    onClick={() => {
                        router.push(borrowerInfo?.id == null ? "/login" : "/borrower-request");
                    }}
                />
            </div>
            <ConfirmModal
                title="알림"
                message="개인정보 수집동의를 하셔야 대여신청이 가능합니다. 수집동의하러 가시겠습니까?"
                onClose={() => setConfirmModal(false)}
                onConfirm={() => router.push(`/borrower-info/agreement/${AGREEMENT_AGREEMENT_VERSION}`)}
                open={confirmModal}
            />
            <AlertModal
                title="알림"
                message={`귀하께서는 다음의 사유로 인해 이용금지 조치 되었습니다.\n \n(사유) ${banReason}`}
                onClose={() => setAlertModal(false)}
                open={alertModal}
            />
        </div>
    );
}
