"use client";

import API_SERVER from "@/apiServer";
import PromptModal from "@/components/utilities/modal/PromptModal";
import BorrowerContext from "@/context/BorrowerContext";
import axios from "axios";
import { useContext, useState } from "react";

export default function MePage() {
    const borrowerContext = useContext(BorrowerContext);
    const [accountPromptModal, setAccountPromptModal] = useState<boolean>(false);
    const [phoneNumberPromptModal, setPhoneNumberPromptModal] = useState<boolean>(false);
    const borrowerInfo = borrowerContext.borrowerInfo;

    const phoneNumberPromptModalOpen = () => {
        setPhoneNumberPromptModal(true);
    };
    const accountNumberPromptModalOpen = () => {
        setAccountPromptModal(true);
    };

    const phoneNumberPromptModalClose = () => {
        setPhoneNumberPromptModal(false);
    };
    const accountNumberPromptModalClose = () => {
        setAccountPromptModal(false);
    };

    const phoneNumberPromptModalConfirm = async (value: string) => {
        if (borrowerInfo?.phoneNumber == null) {
            alert("개인정보 수집동의부터 하시기 바랍니다.");
            setPhoneNumberPromptModal(false);
            return;
        }

        try {
            axios.patch(`${API_SERVER}/borrowers/info/phonenum`, { newPhonenumber: value });
        } catch (error) {
            alert("서버쪽의 에러입니다. 지속될 경우 관리자에게 연락해주세요");
        }
        setPhoneNumberPromptModal(false);
    };
    const accountNumberPromptModalConfirm = async (value: string) => {
        if (borrowerInfo?.phoneNumber == null) {
            alert("개인정보 수집동의부터 하시기 바랍니다.");
            setAccountPromptModal(false);
            return;
        }

        try {
            axios.patch(`${API_SERVER}/borrowers/info/accountNumber`, { newAccountNumber: value });
        } catch (error) {
            alert("서버쪽의 에러입니다. 지속될 경우 관리자에게 연락해주세요");
        }
        setAccountPromptModal(false);
    };

    return (
        <div className="mt-5">
            <p className="black-20px">🙂 나의 정보</p>
            <div className="w-full border border-boxBorder flex flex-col bg-white rounded mt-2">
                <div className="px-5 py-4 border-b border-boxBorder last:border-0 flex flex-col">
                    <p className="bold-18px">이름</p>
                    <p className="regular-16px">{borrowerInfo?.name}</p>
                </div>

                <div className="px-5 py-4 border-b border-boxBorder last:border-0 flex flex-col">
                    <p className="bold-18px">학번</p>
                    <p className="regular-16px">{borrowerInfo?.id}</p>
                </div>

                <div className="px-5 py-4 border-b border-boxBorder last:border-0 flex justify-between items-center">
                    <div className="">
                        <p className="bold-18px">반환계좌</p>
                        <p className="regular-16px">{borrowerInfo?.accountNumber}</p>
                    </div>
                    <button
                        className="py-2 px-4 border border-black rounded cursor-pointer"
                        onClick={phoneNumberPromptModalOpen}
                    >
                        변경
                    </button>
                </div>

                <div className="px-5 py-4 border-b border-boxBorder last:border-0 flex justify-between items-center">
                    <div className="">
                        <p className="bold-18px">전화번호</p>
                        <p className="regular-16px">{borrowerInfo?.phoneNumber}</p>
                    </div>
                    <button
                        className="py-2 px-4 border border-black rounded cursor-pointer"
                        onClick={accountNumberPromptModalOpen}
                    >
                        변경
                    </button>
                </div>

                <div className="px-5 py-4 border-b border-boxBorder last:border-0 flex flex-col">
                    <p className="bold-18px">등록금 납부여부</p>
                    <p className="regular-16px">{borrowerInfo?.verify ? "납부완료" : "인증안함"}</p>
                </div>
            </div>
            <PromptModal
                open={accountPromptModal}
                onClose={accountNumberPromptModalClose}
                title="반환계좌 변겅"
                placeholder="1111-1111-1111-111(은행명) 형식으로 부탁드립니다."
                onConfirm={accountNumberPromptModalConfirm}
            />
            <PromptModal
                open={phoneNumberPromptModal}
                onClose={phoneNumberPromptModalClose}
                title="전화번호 변경"
                placeholder="010-0000-0000 형식으로 부탁드립니다."
                onConfirm={phoneNumberPromptModalConfirm}
            />
        </div>
    );
}
