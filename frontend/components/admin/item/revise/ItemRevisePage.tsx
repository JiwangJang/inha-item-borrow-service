"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import Input from "@/components/utilities/Input";
import Select from "@/components/utilities/select/Select";
import ItemContext from "@/context/ItemContext";
import { ITEM_STATE_TYPE, ItemStateType } from "@/types/ItemStateType";
import itemStatusTypeConvertor from "@/utilities/itemStateTypeConvertor";
import axios from "axios";
import { useSearchParams, useRouter } from "next/navigation";
import { useContext, useState } from "react";

export default function ItemRevisePage() {
    const router = useRouter();
    const itemContext = useContext(ItemContext);
    const itemList = itemContext!.itemList;
    const setItemList = itemContext?.setItemList;

    const param = useSearchParams();
    const id = param.get("id");

    const item = itemList.find((item) => String(item.id) == id)!;

    const { name, location, password, price, deleteReason, state } = item;

    const [newLocation, setNewLocation] = useState(location);
    const [newPassword, setNewPassword] = useState(password);
    const [newState, setNewState] = useState(state);
    const [newDeleteReason, setNewDeleteReason] = useState(deleteReason);

    const onChangeFunc = (value: string) => {
        let state: ItemStateType;
        switch (value) {
            case "대여가능":
                state = ITEM_STATE_TYPE.AFFORD;
                break;
            case "대여중":
                state = ITEM_STATE_TYPE.BORROWED;
                break;
            case "삭제됨":
                state = ITEM_STATE_TYPE.DELETED;
                break;

            default:
                state = ITEM_STATE_TYPE.REVIEWING;
                break;
        }

        setNewState(state);
    };

    const reviseFunc = async () => {
        try {
            if (newLocation == "" || newPassword == "") {
                alert("비치위치와 비밀번호는 공백으로 둘 수 없습니다.");
                return;
            }
            const body = {
                name,
                location: newLocation,
                password: newPassword,
                price,
                state: newState,
            };

            await axios.put(`${API_SERVER}/items/${id}`, body, {
                withCredentials: true,
            });

            if (setItemList)
                setItemList((prev) =>
                    prev?.map((it) =>
                        it.id === item.id
                            ? {
                                  ...it,
                                  location: newLocation,
                                  password: newPassword,
                                  state: newState,
                              }
                            : it,
                    ),
                );

            alert("수정이 완료됐습니다.");
            router.back();
        } catch (error) {
            console.error(error);
            alert(ERROR_MSG.serverError);
        }
    };

    return (
        <div className="mt-5 flex flex-col gap-2">
            <div>
                <p className="mb-1 bold-18px">물품명</p>
                <Input disabled={true} value={name} />
            </div>
            <div>
                <p className="mb-1 bold-18px">비치위치</p>
                <Input value={newLocation} onChange={(e) => setNewLocation(e.target.value)} />
            </div>
            <div>
                <p className="mb-1 bold-18px">비밀번호</p>
                <Input value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
            </div>
            <div>
                <p className="mb-1 bold-18px">물품가격(원)</p>
                <Input disabled={true} value={price} />
            </div>

            {newState == ITEM_STATE_TYPE.DELETED ? (
                <div>
                    <p className="mb-1 bold-18px">물품상태</p>
                    <Select
                        onChange={onChangeFunc}
                        options={["대여가능", "삭제됨"]}
                        value={itemStatusTypeConvertor(newState)}
                    />
                </div>
            ) : null}
            <Button title="저장" className="mt-3 w-full p-2 bold-18px" onClick={reviseFunc} />
        </div>
    );
}
