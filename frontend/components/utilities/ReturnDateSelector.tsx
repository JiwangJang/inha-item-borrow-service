import { useState } from "react";
import Button from "./Button";
import PickerField from "./select/PickerField";
import { dateFormatterForDateInput } from "@/utilities/dateFormatter";

export default function ReturnDateSelector({
    open,
    sendFunc,
    onClose,
    initialReturnAt,
}: {
    open: boolean;
    sendFunc: (returnAtString: string) => Promise<void>;
    onClose: () => void;
    initialReturnAt: string;
}) {
    const [newDate, setNewDate] = useState(dateFormatterForDateInput(initialReturnAt).slice(0, 10));
    const [newTime, setNewTime] = useState(dateFormatterForDateInput(initialReturnAt).slice(11, 16));

    const submit = () => {
        if (newDate == "") {
            alert("날짜는 필수로 선택하셔야 합니다.");
            return;
        }
        sendFunc(`${newDate}T${newTime}:00+09:00`);
        onClose();
    };

    return (
        <div
            className={
                "fixed inset-0 z-50 bg-black/40 transition-opacity duration-200 " +
                (open ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none")
            }
        >
            <div
                className={
                    "bg-back absolute bottom-0 left-1/2 translate-x-[-50%]  w-full max-w-125 py-4 px-4 transition-transform duration-200 rounded-t-xl " +
                    (open ? "translate-y-0" : "translate-y-full")
                }
                onClick={(e) => e.stopPropagation()}
            >
                <div className="mb-3">
                    <p className="black-20px">📅 반납일시 갱신</p>
                    <p>새로운 반납일시를 선택해주세요.</p>
                </div>

                <div className="flex gap-1 mb-3">
                    <PickerField
                        type="date"
                        onChange={(v: string) => setNewDate(v)}
                        placeholder="날짜를 골라주세요"
                        value={newDate}
                    />
                    <PickerField
                        type="time"
                        onChange={(v: string) => setNewTime(v)}
                        placeholder="시간을 골라주세요"
                        value={newTime}
                    />
                </div>
                <div className="flex gap-1">
                    <Button title="닫기" className="w-full py-3 bold-18px bg-placeholder!" onClick={onClose} />
                    <Button title="반납신청하기" className="w-full py-3 bold-18px" onClick={submit} />
                </div>
            </div>
        </div>
    );
}
export function toKstOffsetDateTimeString(epochMilli: number): string {
    const date = new Date(epochMilli);

    // KST로 강제 변환
    const kst = new Date(date.getTime() + 9 * 60 * 60 * 1000);

    const year = kst.getUTCFullYear();
    const month = String(kst.getUTCMonth() + 1).padStart(2, "0");
    const day = String(kst.getUTCDate()).padStart(2, "0");
    const hour = String(kst.getUTCHours()).padStart(2, "0");
    const minute = String(kst.getUTCMinutes()).padStart(2, "0");
    const second = String(kst.getUTCSeconds()).padStart(2, "0");

    return `${year}-${month}-${day}T${hour}:${minute}:${second}+09:00`;
}
