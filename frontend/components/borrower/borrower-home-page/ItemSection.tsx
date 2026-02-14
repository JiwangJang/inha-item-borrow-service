import ItemContext from "@/context/ItemContext";
import ItemInterface from "@/types/ItemInterface";
import { ITEM_STATE_TYPE } from "@/types/ItemStateType";
import { useContext } from "react";

export default function ItemSection() {
    const itemList = useContext(ItemContext).itemList;

    const itemNameList = Array.from(new Set((itemList ?? []).map((it) => it.name)));

    return (
        <div className="mt-2">
            <p className="black-20px">📦 대여물품현황</p>
            {itemNameList.map((itemName, i) => (
                <div className="mt-3" key={i}>
                    <p className="bold-18px">{itemName}</p>
                    <ItemStatusChildren items={itemList} name={itemName} />
                </div>
            ))}
            <div className="mt-2">
                <p className="bold-18px">🤦🏻‍♀️ 생리대</p>
                <p className="regular-16px">학생회 카카오톡 계정으로 문의주세요!</p>
            </div>
        </div>
    );
}

function ItemStatusChildren({ items, name }: { items: ItemInterface[]; name: string }) {
    const targetItems = items.filter((item) => item.name.replaceAll(" ", "") == name);
    const availableItmes = targetItems.filter((item) => item.state == ITEM_STATE_TYPE.AFFORD).length;
    const unavailableItmes = targetItems.filter((item) => item.state == ITEM_STATE_TYPE.BORROWED).length;
    const reviewingItems = targetItems.filter((item) => item.state == ITEM_STATE_TYPE.REVIEWING).length;

    const total = availableItmes + unavailableItmes + reviewingItems;

    return (
        <>
            <p className="mb-1">
                <b>총수량 : {total}</b> (대여가능 : {availableItmes} / 대여중 : {unavailableItmes} / 심사중 :
                {reviewingItems})
            </p>
            <div className="w-full h-8 rounded bg-gray-300 flex overflow-hidden">
                <div className="bg-available h-full" style={{ width: `${(availableItmes / total) * 100}%` }} />
                <div className="bg-unavailable h-full" style={{ width: `${(unavailableItmes / total) * 100}%` }} />
                <div className="bg-amber-200 h-full" style={{ width: `${(reviewingItems / total) * 100}%` }} />
            </div>
        </>
    );
}
