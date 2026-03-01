import { dateFormatter } from "@/utilities/dateFormatter";

export default function StudentCouncilFeeCard({
    borrowerId,
    borrowerName,
    requestAt,
    verify,
    onClick,
}: {
    borrowerId: string;
    borrowerName: string;
    requestAt: string;
    verify: boolean;
    onClick: () => void;
}) {
    return (
        <div
            className="px-5 py-3 bg-white border border-boxBorder rounded-xl cursor-pointer text-center"
            onClick={onClick}
        >
            <p className="bold-18px">{borrowerId}</p>
            <p className="regular-18px">{borrowerName}</p>
            <p className="text-placeholder">{dateFormatter(requestAt)}(신청)</p>
            <p>({verify ? "승인" : "미승인"})</p>
        </div>
    );
}
