"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import Input from "@/components/utilities/Input";
import errorHandler from "@/utilities/errorHandler";
import axios from "axios";
import { useRouter } from "next/navigation";
import { useState } from "react";

const passwordRegExp = new RegExp(
    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+=])[A-Za-z\\d!@#$%^&*()_\\-+=]{9,13}$",
);

export default function PasswordChangePage() {
    const router = useRouter();
    const [originPassword, setOriginPassword] = useState<string>("");
    const [newPassword, setNewPassword] = useState<string>("");
    const [newPasswordAgain, setNewPasswordAgain] = useState<string>("");

    const btnOn = originPassword != "" && passwordRegExp.test(newPassword) && newPassword == newPasswordAgain;

    const changePassword = async () => {
        try {
            const body = {
                newPassword,
                originPassword,
            };

            await axios.patch(`${API_SERVER}/admins/info/password`, body, { withCredentials: true });

            alert("비밀번호 변경이 완료됐습니다.");
            router.back();
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert(
                "알 수 없는 에러가 발생했습니다. 잠시후 다시 시도해보시고, 지속적으로 발생할 경우 개발자에게 연락해주세요.",
            );
            return;
        }
    };

    return (
        <div className="mt-5">
            <p className="black-20px">🔑 비밀번호 변경</p>
            <div className="flex flex-col gap-2 mt-2">
                <div>
                    <p className="bold-16px mb-1">현재 비밀번호</p>
                    <Input
                        placeholder="현재 비밀번호를 입력해주세요"
                        onChange={(e) => setOriginPassword(e.target.value)}
                    />
                </div>
                <div>
                    <p className="bold-16px mb-1">새로운 비밀번호</p>
                    <Input
                        placeholder="새로운 비밀번호를 입력해주세요"
                        onChange={(e) => setNewPassword(e.target.value)}
                    />
                </div>
                <div>
                    <p className="bold-16px mb-1">새로운 비밀번호 재입력</p>
                    <Input
                        placeholder="새로운 비밀번호를 한번 더 입력해주세요"
                        onChange={(e) => setNewPasswordAgain(e.target.value)}
                        onPaste={(e) => {
                            e.preventDefault();
                            alert("붙여넣기는 제한됩니다!");
                        }}
                    />
                </div>
            </div>
            <Button
                title="비밀번호 변경"
                className="py-2 bold-18px w-full mt-3"
                onClick={changePassword}
                disabled={!btnOn}
            />
        </div>
    );
}
