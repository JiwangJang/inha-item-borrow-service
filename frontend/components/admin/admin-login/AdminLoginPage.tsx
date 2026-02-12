"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import Input from "@/components/utilities/Input";
import axios, { AxiosError } from "axios";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { useRef, useState } from "react";

export default function AdminLoginPage() {
    const router = useRouter();
    const [loading, setLoading] = useState<boolean>(false);
    const [passwordInputType, setPasswordInputType] = useState<string>("password");
    const [errorMsg, setErrorMsg] = useState<string>("");
    const idInputRef = useRef<HTMLInputElement>(null);
    const passwordInputRef = useRef<HTMLInputElement>(null);

    const checkboxImage =
        `${passwordInputType == "password" ? "/images/icons/others/inactive" : "/images/icons/others/active"}` +
        "/password.png";

    const checkBoxOnClickFunc = () => {
        if (passwordInputType == "password") {
            setPasswordInputType("text");
        } else {
            setPasswordInputType("password");
        }
    };

    const loginBtnOnclickFunc = async () => {
        if (
            !(idInputRef.current instanceof HTMLInputElement) ||
            !(passwordInputRef.current instanceof HTMLInputElement)
        )
            return;
        setLoading(true);
        const id = idInputRef.current.value;
        const password = passwordInputRef.current.value;

        try {
            await axios.post(
                `${API_SERVER}/admins/login`,
                {
                    id,
                    password,
                },
                { withCredentials: true },
            );
            router.push("/admin");
        } catch (error) {
            if (!(error instanceof AxiosError)) {
                alert("브라우저 오류로 판단됩니다. 새로고침후 다시 시도해주세요.");
                return;
            }
            switch (Number(error.status)) {
                case 400:
                    setErrorMsg("아이디나 비밀번호를 확인하세요.");
                    return;
                default:
                    setErrorMsg("서버쪽의 문제입니다. 지속될 경우 관리자에게 연락바랍니다.");
                    return;
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex flex-col justify-between">
            <div className="mt-5">
                <div className="mb-2">
                    <p className="black-20px">🔐 로그인</p>
                    <p className="regular-14px">해당 페이지는 관리자 전용 페이지입니다.</p>
                </div>
                <div>
                    <Input placeholder="아이디를 입력해주세요" ref={idInputRef} className="mb-1" />
                    <Input
                        placeholder="비밀번호를 입력해주세요"
                        ref={passwordInputRef}
                        type={passwordInputType}
                        onKeyDown={(e) => {
                            if (e.key == "Enter" && !loading) {
                                loginBtnOnclickFunc();
                            }
                        }}
                    />
                </div>
                <div className="flex w-full justify-end items-center mt-2" onClick={checkBoxOnClickFunc}>
                    <span className="regular-14px mr-1 cursor-pointer">비밀번호 보이기</span>
                    <Image
                        src={checkboxImage}
                        width={16}
                        height={16}
                        alt="비밀번호 보이기 버튼"
                        className="cursor-pointer"
                    />
                </div>
            </div>
            <Button
                className="w-full py-3 mt-5 black-20px"
                title="로그인"
                onClick={loginBtnOnclickFunc}
                loading={loading}
            />
            {errorMsg ? <p className="mt-2 text-center bold-16px">{errorMsg}</p> : null}
        </div>
    );
}
