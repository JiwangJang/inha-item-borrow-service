"use client";

import ItemContext from "@/context/ItemContext";
import { useContext, useState } from "react";
import ItemFilter from "./ItemFilter";
import ItemPart from "./ItemPart";
import Image from "next/image";
import { useRouter } from "next/navigation";

export default function ItemManagePage() {
    const [selectItem, setSelectItem] = useState("전체");
    const router = useRouter();

    const itemContext = useContext(ItemContext);
    const itemList = itemContext?.itemList;
    const itemNameList = Array.from(new Set((itemList ?? []).map((it) => it.name)));
    const itemParts: React.ReactNode[] = [];

    const itemFilterOnClick = (name: string) => {
        setSelectItem(name);
    };

    if (selectItem == "전체") {
        itemNameList.forEach((name: string, i: number) => {
            const filteredItems = itemList?.filter((item) => item.name == name) ?? [];
            itemParts.push(<ItemPart name={name} items={filteredItems} key={i} />);
        });
    } else {
        const filteredItems = itemList?.filter((item) => item.name == selectItem) ?? [];
        itemParts.push(<ItemPart name={selectItem} items={filteredItems} />);
    }

    return (
        <div className="mt-5">
            <p className="black-20px">물품관리</p>
            <div className="flex gap-1 mt-3 overflow-x-auto flex-nowrap whitespace-nowrap -mx-3 px-3 scroll-px-3 pb-2">
                <div className="shrink-0">
                    <ItemFilter name="전체" isSelect={"전체" == selectItem} onClick={itemFilterOnClick} />
                </div>

                {itemNameList.map((name) => (
                    <div key={name} className="shrink-0">
                        <ItemFilter name={name} isSelect={name == selectItem} onClick={itemFilterOnClick} />
                    </div>
                ))}
            </div>
            {...itemParts}
            <div className="w-full h-6" />
            <div
                className="fixed max-w-125 w-full top-15 bottom-16 left-1/2 translate-x-[-50%] pointer-events-none"
                onClick={() => {
                    router.push("/admin/item/new");
                }}
            >
                <div className="absolute right-3 bottom-3 bg-black p-3 pointer-events-auto cursor-pointer inline-flex items-center justify-center w-fit rounded-full">
                    <Image src={"/images/icons/others/add_2.svg"} width={32} height={32} alt="아이템 추가버튼" />
                </div>
            </div>
        </div>
    );
}
