"use client";

import BaseModal from "./BaseModal";

export type AlertModalProps = {
    open: boolean;
    onClose: () => void;
    title?: string;
    message: React.ReactNode;
    confirmText?: string;
    onConfirm?: () => void;
};

export default function AlertModal({ open, onClose, title, message, confirmText, onConfirm }: AlertModalProps) {
    return (
        <BaseModal open={open} onClose={onClose} title={title}>
            <div className="regular-16px pb-2 px-3">{message}</div>

            <div className="h-px bg-boxBorder" />

            <button
                type="button"
                className="w-full regular-16px py-2.5 cursor-pointer hover:bg-slate-100"
                onClick={() => {
                    onConfirm?.();
                    onClose();
                }}
            >
                확인
            </button>
        </BaseModal>
    );
}
