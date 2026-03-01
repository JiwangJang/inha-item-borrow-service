import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import PromptModal from "@/components/utilities/modal/PromptModal";
import DivisionContext from "@/context/DivisionContext";
import DivisionInterface from "@/types/DivisionInterface";
import errorHandler from "@/utilities/errorHandler";
import axios from "axios";
import { SetStateAction, useContext, useState } from "react";

export default function DivisionCard({ name, code }: { name: string; code: string }) {
    const { divisionList, setDivisionList } = useContext(DivisionContext)!;
    const [confirmModal, setConfirmModal] = useState(false);
    const [promptModal, setPromptModal] = useState(false);

    const deleteDivision = async () => {
        if (setDivisionList == null) {
            return;
        }

        try {
            await axios.delete(`${API_SERVER}/divisions?division-code=${code}`, { withCredentials: true });
            alert("삭제를 완료하였습니다.");
            setDivisionList((prev) => prev.filter((dv) => dv.code != code));
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert("알 수 없는 에러입니다. 잠시 후 다시 시도해보시고, 오류가 지속될 경우 개발자에게 알려주세요.");
            return;
        }
    };
    const reviseDivisionName = async (newName: string) => {
        if (setDivisionList == null) {
            return;
        }
        try {
            const body = {
                code,
                name: newName,
            };
            await axios.patch(`${API_SERVER}/divisions`, body, { withCredentials: true });
            alert("수정을 완료하였습니다.");
            setDivisionList((prev) => prev.map((dv) => (dv.code === code ? { ...dv, name: newName } : dv)));
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert("알 수 없는 에러입니다. 잠시 후 다시 시도해보시고, 오류가 지속될 경우 개발자에게 알려주세요.");
            return;
        }
    };

    return (
        <div className="bg-white border border-boxBorder rounded-xl p-5 flex flex-col justify-center items-center">
            <p className="bold-18px">{name}</p>
            <p>{code}</p>
            <div className="flex w-full gap-1 mt-2">
                <Button title="삭제" className="flex-1 py-2 bg-alert!" onClick={() => setConfirmModal(true)} />
                <Button title="수정" className="flex-1 py-2" onClick={() => setPromptModal(true)} />
            </div>
            <ConfirmModal
                open={confirmModal}
                message="정말 이 부서를 삭제하시겠어요?"
                onClose={() => setConfirmModal(false)}
                onConfirm={deleteDivision}
                title="경고"
            />
            <PromptModal
                open={promptModal}
                onClose={() => setPromptModal(false)}
                onConfirm={(name) => reviseDivisionName(name)}
                placeholder="새로운 부서명을 입력해주세요"
                title="부서명 변경"
            />
        </div>
    );
}
