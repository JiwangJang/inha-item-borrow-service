"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import Input from "@/components/utilities/Input";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import NoticeContext from "@/context/NoticeContext";
import errorHandler from "@/utilities/errorHandler";
import axios from "axios";
import { notFound, useRouter } from "next/navigation";
import { useContext, useRef, useState } from "react";

export default function AdminNoticeRevisePage({ noticeId }: { noticeId: string }) {
    const { noticeList } = useContext(NoticeContext);
    const current = noticeList.find(({ id }) => String(id) == noticeId);

    if (current == null) {
        return notFound();
    }

    const { title: originTitle, content: originContent } = current;
    const { setNoticeList } = useContext(NoticeContext);
    const [confirmModal, setConfirmModal] = useState(false);
    const titleRef = useRef<HTMLInputElement>(null);
    const contentRef = useRef<HTMLTextAreaElement>(null);
    const router = useRouter();

    const revise = async () => {
        if (
            !(titleRef.current instanceof HTMLInputElement) ||
            !(contentRef.current instanceof HTMLTextAreaElement) ||
            setNoticeList == null
        ) {
            return;
        }

        try {
            const newTitle = titleRef.current.value;
            const newContent = contentRef.current.value;

            const body = {
                newTitle,
                newContent,
            };
            await axios.patch(`${API_SERVER}/notices/${noticeId}`, body, { withCredentials: true });

            setNoticeList((prev) =>
                prev.map((notice) => {
                    if (String(notice.id) == noticeId) {
                        return {
                            ...notice,
                            title: newTitle,
                            content: newContent,
                        };
                    }

                    return notice;
                }),
            );
            alert("공지가 정상적으로 수정되었습니다.");
            router.back();
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert(
                "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해보시고, 지속적으로 발생할 경우 개발자에게 연락해주세요.",
            );
            return;
        }
    };

    return (
        <div className="mt-5">
            <p className="black-20px">🧑‍🔧 공지사항 수정</p>
            <div className="flex flex-col gap-2">
                <div>
                    <p className="bold-18px mb-1">공지제목</p>
                    <Input placeholder="공지제목을 입력해주세요." ref={titleRef} defaultValue={originTitle} />
                </div>
                <div>
                    <p className="bold-18px mb-1">공지내용</p>
                    <textarea
                        className="bg-white border border-boxBorder resize-none w-full p-3 focus:border-black focus:outline-none rounded-md placeholder:text-placeholder"
                        placeholder="공지내용을 입력해주세요."
                        rows={10}
                        defaultValue={originContent}
                        ref={contentRef}
                    />
                </div>
            </div>
            <div className="fixed bottom-0 left-1/2 translate-x-[-50%] max-w-125 w-full px-5 pb-4">
                <Button title="수정" className="py-3 w-full bold-18px" onClick={() => setConfirmModal(true)} />
            </div>
            <ConfirmModal
                open={confirmModal}
                message="이 내용으로 공지를 수정하시겠습니까?"
                onClose={() => setConfirmModal(false)}
                onConfirm={revise}
                title="알림"
            />
        </div>
    );
}
