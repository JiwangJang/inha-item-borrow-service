"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import NoticeContext from "@/context/NoticeContext";
import { dateFormatter } from "@/utilities/dateFormatter";
import errorHandler from "@/utilities/errorHandler";
import positionConvertor from "@/utilities/positionConvertor";
import axios from "axios";
import { notFound, useRouter } from "next/navigation";
import { useContext, useState } from "react";

export default function AdminNoticeSinglePage({ noticeId }: { noticeId: string }) {
    const { noticeList, setNoticeList } = useContext(NoticeContext);
    const [confirmModal, setConfirmModal] = useState(false);
    const router = useRouter();

    const current = noticeList.find((notice) => String(notice.id) == noticeId)!;

    if (current == null) {
        notFound();
    }

    const { id, authorId, title, content, updatedAt, postedAt, adminName, adminPosition } = current;

    const deleteNotice = async () => {
        if (setNoticeList == null) {
            return;
        }
        try {
            await axios.delete(`${API_SERVER}/notices/${id}`, { withCredentials: true });
            setNoticeList((prev) => prev.filter((prevNotice) => prevNotice.id != id));
            alert("삭제가 완료 됐습니다.");
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert(
                "알 수 없는 에러가 발생했습니다. 잠시 후 다시 시도해보시고, 지속적으로 발생하는 경우 개발자에게 연락주세요.",
            );
            return;
        }
    };

    return (
        <div className="bg-white rounded-xl border border-boxBorder py-5 px-4 mt-5">
            <p className="bold-20px">{title}</p>
            <p className="regular-14px">글번호 : {id}</p>
            <p className="regular-14px">
                글쓴이 : {positionConvertor(adminPosition)} {adminName}({authorId})
            </p>
            <p className="text-placeholder regular-14px">
                게시일시 : {dateFormatter(postedAt)}
                {postedAt != updatedAt ? `(${dateFormatter(updatedAt)} 최종수정)` : null}
            </p>
            <p className="leading-tight line-clamp-2 mt-3 whitespace-pre-wrap">{content}</p>
            <div className="flex gap-2 mt-4">
                <Button title="삭제" className="py-2 bg-alert! w-full" onClick={() => setConfirmModal(true)} />
                <Button
                    title="수정"
                    className="py-2 w-full"
                    onClick={() => router.push(`/admin/notice/${id}/revise`)}
                />
            </div>
            <ConfirmModal
                open={confirmModal}
                message="정말 삭제하시겠습니까? 복구가 불가능합니다."
                onClose={() => setConfirmModal(false)}
                onConfirm={deleteNotice}
                title="경고"
            />
        </div>
    );
}
