import NoticeInterface from "./NoticeInterface";

export default interface NoticeContextInterface {
    noticeList: NoticeInterface[];
    setNoticeList: React.Dispatch<React.SetStateAction<NoticeInterface[]>> | null;
}
