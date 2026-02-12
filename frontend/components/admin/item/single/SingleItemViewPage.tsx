"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import InfoRow from "@/components/utilities/InfoRow";
import InfoTable from "@/components/utilities/InfoTable";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import PromptModal from "@/components/utilities/modal/PromptModal";
import Spinner from "@/components/utilities/Spinner";
import ItemContext from "@/context/ItemContext";
import ItemInterface from "@/types/ItemInterface";
import { ITEM_STATE_TYPE } from "@/types/ItemStateType";
import itemStateTypeConvertor from "@/utilities/itemStateTypeConvertor";
import { useRouter } from "next/navigation";
import { useContext, useState } from "react";

export default function SingleItemViewPage({ id }: { id: string }) {
    const router = useRouter();
    const itemContext = useContext(ItemContext);
    const itemList = itemContext?.itemList;
    const item = itemList?.find((item) => String(item.id) == id)!;
    const setItemList = itemContext?.setItemList;

    const [confirmModalOn, setConfirmModalOn] = useState(false);
    const [promptModalOn, setPromptModalOn] = useState(false);
    const [loading, setLoading] = useState(false);

    const stateMsg = itemStateTypeConvertor(item.state);

    const confirmModalOnFunc = () => {
        // 확인 모달 ON
        setConfirmModalOn(true);
    };

    const confirmModalOffFunc = () => {
        // 확인 모달 OFF
        setConfirmModalOn(false);
    };

    const promptModalOnFunc = () => {
        // 프롬프트 모달 ON
        setPromptModalOn(true);
    };

    const promptModalOffFunc = () => {
        // 프롬프트 모달 OFF
        setPromptModalOn(false);
    };

    const deleteFunc = async (value: string) => {
        try {
            if (item.state != ITEM_STATE_TYPE.AFFORD) {
                alert("대여가능한 상품만 삭제가능합니다.");
                return;
            }
            setLoading(true);
            const res = await fetch(`${API_SERVER}/items/${item.id}`, {
                method: "DELETE",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    deleteReason: value,
                }),
            });

            if (res.status != 204) {
                throw new Error();
            }

            if (setItemList) {
                setItemList((prev) =>
                    prev!.map((it) => (it.id === item.id ? { ...it, state: ITEM_STATE_TYPE.DELETED } : it)),
                );
            }

            alert("삭제완료");
            setLoading(false);
            router.back();
        } catch (error) {
            alert("서버에러 발생, 지속될 경우 개발자에게 알려주세요.");
            setLoading(false);
        }
    };

    return (
        <div className="mt-5">
            <InfoTable>
                <InfoRow label="아이디" value={item.id.toString()} />
                <InfoRow label="물품명" value={item.name} />
                <InfoRow label="비치위치" value={item.location ?? ""} />
                <InfoRow label="비밀번호" value={item.password ?? ""} />
                <InfoRow label="물품가격(원)" value={item.price.toString()} />
                <InfoRow label="물품상태" value={stateMsg} />
            </InfoTable>
            <div className="mt-4 flex gap-2">
                <Button
                    title="삭제"
                    className="flex-1 py-3 bold-20px"
                    style={{ backgroundColor: "#DC2626" }}
                    onClick={confirmModalOnFunc}
                />
                <Button
                    title="수정"
                    className="flex-1 py-3 bold-20px"
                    onClick={() => router.push(`/admin/item/revise?id=${item.id}`)}
                />
            </div>
            <ConfirmModal
                title="경고"
                open={confirmModalOn}
                onClose={confirmModalOffFunc}
                onConfirm={promptModalOnFunc}
                message={"정말 삭제하시겠어요?"}
            />
            <PromptModal
                onClose={promptModalOffFunc}
                open={promptModalOn}
                title="사유입력"
                placeholder="삭제 사유를 입력해주세요."
                onConfirm={deleteFunc}
            />
            {loading ? (
                <div className="fixed z-100 top-0 left-0 w-full h-screen bg-black/40 flex flex-col justify-center items-center gap-4">
                    <Spinner size={120} thickness={8} />
                    <p className="text-white bold-20px">요청하신 작업 처리중..</p>
                </div>
            ) : null}
        </div>
    );
}
