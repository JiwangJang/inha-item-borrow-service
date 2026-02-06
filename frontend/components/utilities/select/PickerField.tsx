"use client";

import { useRef } from "react";
import FieldBox from "./FieldBox";

export default function PickerField({
    value,
    onChange,
    placeholder,
    type, // "date" | "time"
    min,
    max,
}: {
    value: string;
    onChange: (v: string) => void;
    placeholder: string;
    type: "date" | "time";
    min?: string;
    max?: string;
}) {
    const inputRef = useRef<HTMLInputElement>(null);

    const openPicker = () => {
        const el = inputRef.current;
        if (!el) return;

        // Chrome/Edge는 showPicker 지원. Safari는 focus로 충분.
        if (typeof el.showPicker === "function") el.showPicker();
        else {
            el.focus();
            el.click();
        }
    };

    return (
        <div className="relative w-full" onClick={openPicker}>
            <button type="button" className="w-full text-left">
                <FieldBox inactive={value == ""}>
                    <span className={value ? "text-slate-900" : "text-placeholder"}>{value || placeholder}</span>
                </FieldBox>
            </button>

            <input
                ref={inputRef}
                type={type}
                value={value}
                onChange={(e) => onChange(e.target.value)}
                min={min}
                max={max}
                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                aria-label={placeholder}
            />
        </div>
    );
}
