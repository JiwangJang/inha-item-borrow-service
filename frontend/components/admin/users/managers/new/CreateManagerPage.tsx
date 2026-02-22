"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import Input from "@/components/utilities/Input";
import Select from "@/components/utilities/select/Select";
import AdminListContext from "@/context/AdminListContext";
import DivisionContext from "@/context/DivisionContext";
import { ADMIN_POSITION_TYPE } from "@/types/AdminPositionType";
import errorHandler from "@/utilities/errorHandler";
import axios from "axios";
import { useRouter } from "next/navigation";
import { useContext, useRef, useState } from "react";

export default function CreateManagerPage() {
    const { setAdminList } = useContext(AdminListContext);
    const [divisionSelect, setDivisionSelect] = useState<string>("");
    const [positionSelect, setPositionSelect] = useState<string>("");
    const [newAdminId, setNewAdminId] = useState<string>("");

    const nameRef = useRef<HTMLInputElement>(null);

    const router = useRouter();

    const { divisionList } = useContext(DivisionContext)!;
    const positoinList = [
        {
            code: ADMIN_POSITION_TYPE.VICE_PRESIDENT,
            name: "학생부회장",
        },
        {
            code: ADMIN_POSITION_TYPE.DIVISION_HEAD,
            name: "국장",
        },
        {
            code: ADMIN_POSITION_TYPE.DIVISION_MEMBER,
            name: "일반국원",
        },
    ];
    const isValidId = new RegExp("^[a-zA-Z0-9]{4,10}$").test(newAdminId);

    const btnOn = isValidId && divisionSelect != "" && positionSelect != "" && nameRef.current?.value != "";

    const register = async () => {
        if (!(nameRef.current instanceof HTMLInputElement) || setAdminList == null) {
            alert("새로고침 후 다시 시도해주세요");
            return;
        }

        if (!isValidId) {
            alert("아이디 형식을 다시 확인해주세요!");
            return;
        }

        try {
            const position = positoinList.find(({ name }) => name == positionSelect)?.code!;
            const division = divisionList.find(({ name }) => name == divisionSelect)?.code!;
            const name = nameRef.current.value;
            const body = {
                id: newAdminId,
                name,
                position,
                division,
            };

            await axios.post(`${API_SERVER}/admins/sub-admin`, body, { withCredentials: true });

            setAdminList((prev) =>
                prev.concat([
                    {
                        id: newAdminId,
                        divisionCode: division,
                        name,
                        position,
                    },
                ]),
            );

            alert("등록이 완료됐습니다.");
            router.back();
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert(
                "알 수 없는 에러가 발생했습니다. 잠시후 다시 시도해보시고, 지속적으로 발생할 경우 개발자에게 연락해주세요.",
            );
            console.error(error);
            return;
        }
    };

    return (
        <div className="mt-5 flex flex-col gap-2">
            <div>
                <p className="bold-18px mt-2">1. 신규 관리자 아이디</p>
                <Input
                    value={newAdminId}
                    onChange={(e) => {
                        setNewAdminId(e.target.value);
                    }}
                    placeholder="아이디를 입력해주세요"
                />
                <p className="pl-0.5 mt-1 regular-14px text-placeholder">
                    아이디는 영어대소문자와 숫자로 4~10자여야 합니다.
                </p>
            </div>
            <div>
                <p className="bold-18px mt-2">2. 신규 관리자 이름</p>
                <Input ref={nameRef} placeholder="이름을 입력해주세요" />
            </div>
            <div>
                <p className="bold-18px mt-2">3. 신규 관리자 부서</p>
                <Select
                    options={divisionList.map(({ name }) => name)}
                    onChange={(value) => {
                        setDivisionSelect(value);
                    }}
                    value={divisionSelect}
                    placeholder="부서를 선택해주세요"
                />
            </div>
            <div>
                <p className="bold-18px mt-2">4. 신규 관리자 직급</p>
                <Select
                    options={positoinList.map(({ name }) => name)}
                    onChange={(value) => {
                        setPositionSelect(value);
                    }}
                    value={positionSelect}
                    placeholder="직급을 선택해주세요"
                />
            </div>
            <Button title="등록하기" className="py-2.5 bold-18px mt-2" disabled={!btnOn} onClick={register} />
        </div>
    );
}
