import NoticeContext from "@/context/NoticeContext";
import { mockNotices } from "@/mockData/mockNotices";
import { dateFormatter } from "@/utilities/dateFormatter";
import Link from "next/link";
import { useContext } from "react";

export default function NoticeSection() {
    const { noticeList } = useContext(NoticeContext);

    noticeList.sort((a, b) => new Date(b.postedAt).getTime() - new Date(a.postedAt).getTime());
    const exposeNotice = noticeList.slice(0, 3);

    return (
        <div>
            <p className="black-20px">🔔 공지사항</p>
            <div className="w-full mt-3 flex flex-col bg-white border-boxBorder border rounded overflow-hidden">
                {exposeNotice.map((notice, i) => (
                    <Link href={`/notice/${notice.id}`} key={i}>
                        <div className="w-full flex-1 px-3 py-2 border-b border-boxBorder last:border-0">
                            <div className="flex justify-between items-end">
                                <p className="flex-1 bold-16px">{notice.title}</p>
                                <p className="text-placeholder regular-14px">{dateFormatter(notice.updatedAt)}</p>
                            </div>
                            <p className="truncate text-elipsis regular-14px mt-1">{notice.content}</p>
                        </div>
                    </Link>
                ))}
            </div>
            <div className="w-full flex justify-center">
                <Link href={"/notice"} className="mt-3 bg-black text-white py-0.5 px-4 rounded-full regular-16px">
                    더보기
                </Link>
            </div>
        </div>
    );
}
