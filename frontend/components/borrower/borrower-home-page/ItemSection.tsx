import ItemContext from "@/context/ItemContext";
import mockItems from "@/mockData/mockItems";
import ItemInterface from "@/types/ItemInterface";
import { ITEM_STATE_TYPE } from "@/types/ItemStateType";
import { useContext } from "react";

export default function ItemSection() {
    const items = useContext(ItemContext);

    return (
        <div className="mt-2">
            <p className="black-20px">📦 대여물품현황</p>
            <div className="mt-3">
                <p className="bold-18px">☂ ️접이식 우산</p>
                <ItemStatusChildren items={mockItems} name="접이식우산" />
            </div>
            <div className="mt-2">
                <p className="bold-18px">🌂 장우산</p>
                <ItemStatusChildren items={mockItems} name="장우산" />
            </div>
            <div className="mt-2">
                <p className="bold-18px">🧮 공학용 계산기</p>
                <ItemStatusChildren items={mockItems} name="공학용계산기" />
            </div>
            <div className="mt-2">
                <p className="bold-18px">🔋 보조배터리</p>
                <ItemStatusChildren items={mockItems} name="보조배터리" />
            </div>
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
