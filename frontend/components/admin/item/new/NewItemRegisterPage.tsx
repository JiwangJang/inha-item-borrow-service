"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import Input from "@/components/utilities/Input";
import Select from "@/components/utilities/select/Select";
import ItemContext from "@/context/ItemContext";
import axios from "axios";
import { useContext, useRef, useState } from "react";

export default function NewItemRegisterPage() {
    const [isNewItem, setIsNewItem] = useState<boolean>(false);
    const [selectedItem, setSelectedItem] = useState("");
    const [itemLocation, setItemLocation] = useState("");
    const [itemPassword, setItemPassword] = useState("");
    const [itemPrice, setItemPrice] = useState("");
    const { itemList, setItemList } = useContext(ItemContext);

    const itemNameList = Array.from(new Set((itemList ?? []).map((it) => it.name)));
    const itemNameInputRef = useRef<HTMLInputElement>(null);
    const locationInputRef = useRef<HTMLInputElement>(null);
    const passwordInputRef = useRef<HTMLInputElement>(null);
    const priceInputRef = useRef<HTMLInputElement>(null);
    // 기존거에서 이름만 가져온다 -> 셀렉트로 보여주고, 마지막에 새로운 아이템 추가하는 경우에만 인풋 보여주기
    // 새로운거 추가하면 context에 반영
    itemNameList.push("신규 대여물품 등록");

    const selectOnChange = (value: string) => {
        if (value == "신규 대여물품 등록") {
            setSelectedItem("");
            setItemPrice("");
            setIsNewItem(true);
        } else {
            setSelectedItem(value);
            const price = itemList?.find((item) => item.name == value)?.price;
            if (price) {
                setItemPrice(String(price));
            }
        }
    };

    const resetButton = () => {
        setIsNewItem(false);
        setSelectedItem("");
        setItemPrice("");
        setItemLocation("");
        setItemPassword("");
    };

    const buttonOnClick = async () => {
        try {
            // 띄어쓰기 제거
            const name = isNewItem ? itemNameInputRef.current?.value.replaceAll(" ", "") : selectedItem;
            const location = locationInputRef.current?.value;
            const password = passwordInputRef.current?.value;
            const price = priceInputRef.current?.value;

            if (name == "" || location == "" || password == "" || price == "") {
                alert("올바른 값을 입력해주세요.");
                return;
            }

            if (!Number(price) || !Number(password)) {
                alert("가격과 비밀번호는 숫자만 입력하셔야합니다.");
                return;
            }

            const body = {
                name,
                location,
                password,
                price,
            };

            const res = await axios.post(`${API_SERVER}/items`, body, {
                withCredentials: true,
            });

            if (setItemList) {
                setItemList((prev) => prev.concat(res.data.data));
            }

            alert("물품이 정상적으로 등록되었습니다.");
            resetButton();
        } catch (error) {
            console.error(error);
            alert("서버쪽 에러입니다.");
        }
    };

    return (
        <div className="mt-5 flex flex-col gap-2">
            <div>
                <p className="bold-18px mb-1">물품명</p>
                {isNewItem ? (
                    <>
                        <Input placeholder="대여물품 이름을 입력해주세요" ref={itemNameInputRef} />
                        <span className="mt-1 pl-1 underline text-blue-500 cursor-pointer" onClick={resetButton}>
                            다시고르기
                        </span>
                    </>
                ) : (
                    <Select
                        value={selectedItem}
                        onChange={selectOnChange}
                        placeholder="등록할 물품을 골라주세요"
                        options={itemNameList}
                    />
                )}
            </div>
            <div>
                <p className="bold-18px mb-1">비치위치</p>
                <Input
                    placeholder="대여물품 비치위치를 입력해주세요"
                    value={itemLocation}
                    onChange={(e) => {
                        setItemLocation(e.target.value);
                    }}
                    ref={locationInputRef}
                />
            </div>
            <div>
                <p className="bold-18px mb-1">비밀번호</p>
                <Input
                    type="text"
                    inputMode="numeric"
                    pattern="[0-9]*"
                    placeholder="대여물품 보관 비밀번호를 입력해주세요"
                    value={itemPassword}
                    onChange={(e) => {
                        const onlyNumber = e.target.value.replace(/[^0-9]/g, "");
                        setItemPassword(onlyNumber);
                    }}
                    ref={passwordInputRef}
                />
            </div>
            <div>
                <p className="bold-18px mb-1">가격(원)</p>
                <Input
                    type="text"
                    inputMode="numeric"
                    pattern="[0-9]*"
                    placeholder={
                        isNewItem ? "대여물품 가격을 입력해주세요" : "대여물품을 선택하면 자동으로 입력됩니다."
                    }
                    ref={priceInputRef}
                    value={itemPrice}
                    onChange={(e) => {
                        const onlyNumber = e.target.value.replace(/[^0-9]/g, "");
                        setItemPrice(onlyNumber);
                    }}
                    disabled={!isNewItem}
                />
            </div>
            <Button title="등록" className="w-full py-2 bold-20px" onClick={buttonOnClick} />
        </div>
    );
}
