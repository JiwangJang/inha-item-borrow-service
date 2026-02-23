"use client";

import NoticeContext from "@/context/NoticeContext";
import { useContext } from "react";
import AdminNoticeListItem from "./AdminNoticeListItem";
import Button from "@/components/utilities/Button";
import { useRouter } from "next/navigation";

export default function AdminNoticeListPage() {
    const { noticeList } = useContext(NoticeContext);
    const router = useRouter();

    return (
        <div className="mt-5 pb-3">
            <div>
                <p className="black-20px">📣 공지사항</p>
                <p>학생들에게 좋은소식을 전달해봐요!</p>
            </div>
            <div className="mt-3 flex flex-col gap-1">
                {noticeList.map((notice) => (
                    <AdminNoticeListItem notice={notice} key={notice.id} />
                ))}
            </div>
            <div className="fixed bottom-0 left-1/2 translate-x-[-50%]  max-w-125 w-full px-5 pb-3">
                <Button
                    title="새 공지등록"
                    className="w-full py-3 bold-18px"
                    onClick={() => router.push("/admin/notice/new")}
                />
            </div>
        </div>
    );
}
