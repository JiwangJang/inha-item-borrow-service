"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import Input from "@/components/utilities/Input";
import DivisionContext from "@/context/DivisionContext";
import axios from "axios";
import { useContext, useRef } from "react";

export default function CreateDivisionPage() {
    const { setDivisionList } = useContext(DivisionContext)!;
    const nameRef = useRef<HTMLInputElement>(null);
    const codeRef = useRef<HTMLInputElement>(null);

    const handleCodeChange = (value: string) => {
        const onlyLetters = value.replace(/[^a-zA-Z]/g, "");
        if (codeRef.current) {
            codeRef.current.value = onlyLetters.toUpperCase();
        }
    };

    const register = async () => {
        if (setDivisionList == null) {
            return;
        }
        const name = nameRef.current?.value ?? "";
        const code = codeRef.current?.value ?? "";

        if (!name.trim()) {
            alert("부서명을 입력해주세요.");
            return;
        }

        if (!code.trim()) {
            alert("부서코드를 입력해주세요.");
            return;
        }

        try {
            await axios.post(
                `${API_SERVER}/divisions`,
                {
                    code,
                    name: name.trim(),
                },
                { withCredentials: true },
            );

            alert("부서 등록이 완료되었습니다.");

            if (nameRef.current) nameRef.current.value = "";
            if (codeRef.current) codeRef.current.value = "";
            setDivisionList((prev) =>
                prev.concat({
                    code,
                    name: name.trim(),
                }),
            );
        } catch (error) {
            if (axios.isAxiosError(error)) {
                alert(error.response?.data?.message ?? "부서 등록 중 오류가 발생했습니다.");
            } else {
                alert("알 수 없는 오류가 발생했습니다.");
            }
        }
    };

    return (
        <div className="mt-5">
            <p className="black-20px mb-2">🆕 신규 부서 등록</p>
            <div className="flex flex-col gap-2">
                <div>
                    <p className="mb-1 bold-18px">부서명</p>
                    <Input placeholder="부서명을 입력해주세요" ref={nameRef} />
                </div>
                <div>
                    <p className="mb-1 bold-18px">부서코드</p>
                    <Input
                        placeholder="부서코드를 입력해주세요"
                        ref={codeRef}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleCodeChange(e.target.value)}
                    />
                    <p className="mt-1 text-placeholder">부서코드는 영어 대문자만 허용됩니다.</p>
                </div>
            </div>
            <div className="fixed w-full max-w-125 bottom-0 left-1/2 translate-x-[-50%] px-5 pb-5">
                <Button title={"등록하기"} className="w-full py-3 bold-18px" onClick={register} />
            </div>
        </div>
    );
}
