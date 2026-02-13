"use client";

import Select from "@/components/utilities/select/Select";
import { useContext, useState } from "react";
import ItemBorrowConditions from "./ItemBorrowConditions";
import ItemContext from "@/context/ItemContext";
import Image from "next/image";
import PickerField from "@/components/utilities/select/PickerField";
import Button from "@/components/utilities/Button";
import { ITEM_STATUS_TYPE } from "@/types/ItemStateType";
import axios from "axios";
import API_SERVER from "@/apiServer";

export default function BorrowerRequestPage() {
    const itemContext = useContext(ItemContext);
    const itemList = itemContext?.itemList;
    const setItemList = itemContext?.setItemList;

    const [item, setItem] = useState<string>("");
    const [agree, setAgree] = useState<boolean>(false);
    const [buttonOn, setButtonOn] = useState<boolean>(false);
    const [borrowDate, setBorrowDate] = useState<string>("");
    const [borrowTime, setBorrowTime] = useState<string>("");
    const [returnDate, setReturnDate] = useState<string>("");
    // 사용자는 카테고리만 고르고 클라이언트 정보에서 빌릴 수 있는거 요청 넣고, 안되면 다른 아이디로 요청넣기

    const checkboxImage =
        `${agree ? "/images/icons/others/active" : "/images/icons/others/inactive"}` + "/password.png";
    const itemNameList = Array.from(new Set((itemList ?? []).map((it) => it.name)));
    const itemNamePriceObj = (itemList ?? []).reduce(
        (acc, cur) => {
            if (acc[cur.name] == null) acc[cur.name] = cur.price;
            return acc;
        },
        {} as Record<string, number>,
    );

    const checkButtonCanOn = () => {
        console.log(agree && borrowDate != "" && borrowTime != "" && returnDate != "" && item != "");
        return agree && borrowDate != "" && borrowTime != "" && returnDate != "" && item != "";
    };

    const itemSelectOnChange = (value: string) => {
        setItem(value);
        if (checkButtonCanOn()) setButtonOn(true);
        else if (!checkButtonCanOn() && buttonOn) setButtonOn(false);
    };

    const checkBoxOnClickFunc = () => {
        setAgree(!agree);
        if (checkButtonCanOn()) setButtonOn(true);
        else if (!checkButtonCanOn() && buttonOn) setButtonOn(false);
    };

    const borrowDateFieldOnChange = (value: string) => {
        setBorrowDate(value);
        if (checkButtonCanOn()) setButtonOn(true);
        else if (!checkButtonCanOn() && buttonOn) setButtonOn(false);
    };

    const borrowTimeFieldOnChange = (value: string) => {
        setBorrowTime(value);
        if (checkButtonCanOn()) setButtonOn(true);
        else if (!checkButtonCanOn() && buttonOn) setButtonOn(false);
    };

    const returnDateFieldOnChange = (value: string) => {
        setReturnDate(value);
        if (checkButtonCanOn()) setButtonOn(true);
        else if (!checkButtonCanOn() && buttonOn) setButtonOn(false);
    };

    const buttonOnclick = async () => {
        try {
            if (itemList?.length == 0) return;
            const selectedItemId = itemList?.find((it) => it.status == ITEM_STATUS_TYPE.AFFORD && it.name == item)?.id;
            const body = {
                itemId: selectedItemId,
                borrowerAt: new Date(borrowDate + " " + borrowTime).toISOString(),
                returnAt: new Date(returnDate).toISOString(),
                type: "BORROW",
            };

            // const res = await axios.post(`${API_SERVER}/requests`, body, {
            //     withCredentials: true,
            // });

            // const result = res.data;
            // 응답으로 준 생성시간하고 아이디  그리고 사용자가 입력한 정보를 기반으로 request객체 만들기

            console.log(body);
        } catch (error) {
            console.error(error);
            alert("에러");
        }
    };

    return (
        <div className="mt-5">
            <div>
                <p className="bold-18px mb-2">1. 물품대여조건</p>
                <ItemBorrowConditions />
                <div className="flex mt-2">
                    <p className="flex-1 bold-16px">위 내용을 숙지하셨습니까?</p>
                    <div className="flex justify-end items-center cursor-pointer" onClick={checkBoxOnClickFunc}>
                        <span className="regular-16px mr-1 cursor-pointer pl-2">숙지했습니다.</span>
                        <Image src={checkboxImage} width={16} height={16} alt="체크박스" className="cursor-pointer" />
                    </div>
                </div>
            </div>

            <div className="mt-3">
                <p className="bold-18px mb-2">2. 대여물품을 선택해주세요</p>
                <Select value={item} options={itemNameList} placeholder="대여물품 선택" onChange={itemSelectOnChange} />
                {item != "" ? (
                    <p className="mt-2 pl-0.5 bold-16px">
                        보증금 {Math.floor((itemNamePriceObj[item] * 0.3) / 1000) * 1000}원을 입금해주세요.
                    </p>
                ) : null}
            </div>

            <div className="mt-3">
                <p className="bold-18px mb-2">3. 대여일시를 선택해주세요</p>
                <div className="flex gap-2">
                    <PickerField
                        type="date"
                        placeholder="대여일 선택"
                        onChange={borrowDateFieldOnChange}
                        value={borrowDate}
                    />
                    <PickerField
                        type="time"
                        placeholder="대여시간 선택"
                        onChange={borrowTimeFieldOnChange}
                        value={borrowTime}
                    />
                </div>
            </div>

            <div className="mt-3">
                <p className="bold-18px mb-2">4. 반납일을 선택해주세요</p>
                <div className="mb-2">
                    <PickerField
                        type="date"
                        placeholder="반납일 선택"
                        onChange={returnDateFieldOnChange}
                        value={returnDate}
                    />
                </div>
                <p className="text-placeholder pl-0.5 leading-tight">
                    대여 기간은 원칙적으로 대여한 날로부터 최대 7일로 하며, 공휴일 또는 학생회의 사정으로 대여 및 반납이
                    곤란한 경우, 대여자의 특별한 사정으로 학생회와 사전에 협의된 경우 합리적인 선에서 조정할 수 있음.
                </p>
            </div>

            <Button
                title="대여신청"
                className="w-full p-3 bold-18px mt-6"
                disabled={buttonOn}
                onClick={buttonOnclick}
            />
        </div>
    );
}
