import { ItemStateType, ITEM_STATE_TYPE } from "@/types/ItemStateType";

export default function EachItem({ id, status }: { id: number; status: ItemStateType }) {
    const convertFunc = (code: string) => {
        switch (code) {
            case ITEM_STATE_TYPE.AFFORD:
                return "대여가능";
            case ITEM_STATE_TYPE.BORROWED:
                return "대여중";
            case ITEM_STATE_TYPE.REVIEWING:
                return "검토중";
            default:
                return "대여가능";
        }
    };

    return (
        <div className="rounded-xl overflow-hidden border-2 border-boxBorder h-26 flex flex-col transition-shadow duration-200 hover:shadow-lg hover:-translate-y-1 transform">
            <div className="bg-black text-white text-center py-0.5">ID: {id}</div>
            <div className="flex justify-center items-center flex-1 bg-white">{convertFunc(status)}</div>
        </div>
    );
}
