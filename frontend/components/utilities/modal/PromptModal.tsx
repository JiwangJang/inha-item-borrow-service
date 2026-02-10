"use client";

import { useEffect, useState } from "react";
import BaseModal from "./BaseModal";

export type PromptModalProps = {
    open: boolean;
    onClose: () => void;
    title: string;
    placeholder: string;
    /** 입력값을 넘겨줌 */
    onConfirm: (value: string) => void;
    onCancel?: () => void;

    /** 비밀번호 같은 경우 외부에서 type 지정 가능 */
    inputType?: React.InputHTMLAttributes<HTMLInputElement>["type"];
};

export default function PromptModal({ open, onClose, title, placeholder, onConfirm, onCancel }: PromptModalProps) {
    const [value, setValue] = useState("");

    // 열릴 때마다 초기화(원하면 제거 가능)
    useEffect(() => {
        if (open) setValue("");
    }, [open]);

    return (
        <BaseModal open={open} onClose={onClose} title={title} closeOnBackdrop={false}>
            <div className="mt-2 mx-2">
                <textarea
                    value={value}
                    onChange={(e) => setValue(e.target.value)}
                    placeholder={placeholder}
                    className="w-full px-3 py-2 bg-back resize-none rounded"
                    rows={3}
                />
            </div>

            <div className="mt-2 h-px bg-boxBorder" />

            <div className="flex">
                <button
                    type="button"
                    className="py-2.5 regular-16px text-placeholder hover:bg-slate-100 flex-1 cursor-pointer"
                    onClick={() => {
                        onCancel?.();
                        onClose();
                    }}
                >
                    취소
                </button>

                <div className="w-px bg-boxBorder" />

                <button
                    type="button"
                    className="py-2.5 regular-16px text-slate-900 hover:bg-slate-100 flex-1 cursor-pointer"
                    onClick={() => {
                        onConfirm(value);
                        onClose();
                    }}
                >
                    확인
                </button>
            </div>
        </BaseModal>
    );
}
