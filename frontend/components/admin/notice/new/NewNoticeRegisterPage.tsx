"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import Input from "@/components/utilities/Input";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import { toKstOffsetDateTimeString } from "@/components/utilities/ReturnDateSelector";
import AdminContext from "@/context/AdminContext";
import NoticeContext from "@/context/NoticeContext";
import errorHandler from "@/utilities/errorHandler";
import axios from "axios";
import { useRouter } from "next/navigation";
import { useContext, useRef, useState } from "react";

export default function NewNoticeRegisterPage() {
    const { adminInfo } = useContext(AdminContext);
    const { setNoticeList } = useContext(NoticeContext);
    const [confirmModal, setConfirmModal] = useState(false);
    const titleRef = useRef<HTMLInputElement>(null);
    const contentRef = useRef<HTMLTextAreaElement>(null);
    const router = useRouter();

    const register = async () => {
        if (
            !(titleRef.current instanceof HTMLInputElement) ||
            !(contentRef.current instanceof HTMLTextAreaElement) ||
            setNoticeList == null
        ) {
            return;
        }

        try {
            const title = titleRef.current.value;
            const content = contentRef.current.value;
            const body = {
                title,
                content,
            };

            const res = await axios.post(`${API_SERVER}/notices`, body, { withCredentials: true });
            const { id } = res.data.data;

            setNoticeList((prev) =>
                prev.concat({
                    authorId: adminInfo!.id,
                    content,
                    title,
                    id,
                    postedAt: toKstOffsetDateTimeString(new Date().getTime()),
                    updatedAt: toKstOffsetDateTimeString(new Date().getTime()),
                }),
            );
            alert("공지 등록이 완료됐습니다.");
            router.back();
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert("알 수 없는 에러가 발생했습니다. 지속적으로 발생할 경우 개발자에게 연락해주세요.");
            console.error(error);
            return;
        }
    };

    return (
        <div className="mt-5">
            <p className="black-20px mb-3">✏️ 새 공지등록</p>
            <div className="flex flex-col gap-2">
                <div>
                    <p className="bold-18px mb-1">공지제목</p>
                    <Input placeholder="공지제목을 입력해주세요." ref={titleRef} />
                </div>
                <div>
                    <p className="bold-18px mb-1">공지내용</p>
                    <textarea
                        className="bg-white border border-boxBorder resize-none w-full p-3 focus:border-black focus:outline-none rounded-md placeholder:text-placeholder"
                        placeholder="공지내용을 입력해주세요."
                        rows={10}
                        ref={contentRef}
                    />
                </div>
            </div>
            <div className="fixed bottom-0 left-1/2 translate-x-[-50%] max-w-125 w-full px-5 pb-4">
                <Button title="등록" className="py-3 w-full bold-18px" onClick={() => setConfirmModal(true)} />
            </div>
            <ConfirmModal
                open={confirmModal}
                message="이 내용으로 공지를 등록하시겠습니까?"
                onClose={() => setConfirmModal(false)}
                onConfirm={register}
                title="알림"
            />
        </div>
    );
}
