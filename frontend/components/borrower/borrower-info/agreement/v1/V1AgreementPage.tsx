"use client";

import axios from "axios";
import BorrowerContext from "@/context/BorrowerContext";
import { useContext, useRef, useState } from "react";
import AgreementSection from "./V1AgreementSection";
import Image from "next/image";
import Input from "@/components/utilities/Input";
import Button from "@/components/utilities/Button";
import API_SERVER from "@/apiServer";
import AGREEMENT_AGREEMENT_VERSION from "@/utilities/agreementVersion";

export default function V1AgreementPage() {
    const borrowerContext = useContext(BorrowerContext);
    const [isAgree, setIsAgree] = useState(false);
    const [phoneNumber, setPhoneNumber] = useState<string>("");
    const [accountNumber, setAccountNumber] = useState<string>("");
    const isAgreeBefore = borrowerContext.borrowerInfo?.agreementVersion != null;
    const phoneDigits = phoneNumber.replace(/\D/g, "");
    const isValidPhoneNumber = /^01[0-9]\d{7,8}$/.test(phoneDigits);
    const buttonOn = isValidPhoneNumber && isAgree && accountNumber != "";

    const checkboxImage =
        `${isAgree ? "/images/icons/others/active" : "/images/icons/others/inactive"}` + "/password.png";

    const checkBoxOnClickFunc = () => {
        if (isAgree) {
            setIsAgree(false);
        } else {
            setIsAgree(true);
        }
    };

    const handlePhoneNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        let value = e.target.value.replace(/\D/g, "");

        if (value.length <= 3) {
            setPhoneNumber(value);
        } else if (value.length <= 7) {
            setPhoneNumber(`${value.slice(0, 3)}-${value.slice(3)}`);
        } else {
            setPhoneNumber(`${value.slice(0, 3)}-${value.slice(3, 7)}-${value.slice(7, 11)}`);
        }
    };

    const handleSubmit = async () => {
        if (!buttonOn) {
            return;
        }

        try {
            await axios.post(
                `${API_SERVER}/agreement`,
                {
                    version: AGREEMENT_AGREEMENT_VERSION,
                    phoneNumber,
                    accountNumber,
                },
                {
                    withCredentials: true,
                },
            );

            if (borrowerContext.setBorrowerInfo != null && borrowerContext.borrowerInfo) {
                borrowerContext.setBorrowerInfo({
                    ...borrowerContext.borrowerInfo,
                    phoneNumber,
                    accountNumber,
                    agreementVersion: AGREEMENT_AGREEMENT_VERSION,
                });
            }

            alert("정상적으로 제출이 완료됐습니다.");
        } catch (error) {
            console.error("개인정보 제출 실패:", error);
            alert("서버측의 오류입니다. 잠시후 다시 시도해주시고, 계속될 경우 관리자에게 연락해주시기 바랍니다.");
        }
    };

    return (
        <div className="mt-5">
            {borrowerContext.borrowerInfo?.agreementVersion == null ? (
                <p className="black-20px mb-3">1. 개인정보 수집 및 이용 동의</p>
            ) : null}

            <AgreementSection />
            {isAgreeBefore ? (
                <p className="mt-3 text-center">위 방침에 따른 개인정보 수집 및 이용에 동의하셨습니다.</p>
            ) : (
                <div className="flex w-full justify-between my-3">
                    <p className="regular-16px">본인의 개인정보 수집 및 이용에 동의하십니까?</p>
                    <div className="flex justify-end items-center" onClick={checkBoxOnClickFunc}>
                        <span className="bold-16px mr-1 cursor-pointer">동의하기</span>
                        <Image src={checkboxImage} width={16} height={16} alt="동의 버튼" className="cursor-pointer" />
                    </div>
                </div>
            )}

            {isAgreeBefore ? null : (
                <>
                    <p className="black-20px mb-3">2. 전화번호 및 반환계좌 입력</p>
                    <div>
                        <p className="mb-1 bold-16px">전화번호</p>
                        <Input
                            placeholder="전화번호를 입력해주세요"
                            onChange={handlePhoneNumberChange}
                            value={phoneNumber}
                        />
                    </div>
                    <div className="mt-2">
                        <p className="mb-1 bold-16px">반환계좌</p>
                        <Input
                            placeholder="1111-1111-1111-11(은행명) 형식으로 입력해주세요"
                            value={accountNumber}
                            onChange={(e) => setAccountNumber(e.target.value)}
                        />
                    </div>
                    <Button
                        title="개인정보 동의 및 제출"
                        className="w-full py-3 mt-4"
                        disabled={!buttonOn}
                        onClick={handleSubmit}
                    />
                </>
            )}
        </div>
    );
}
