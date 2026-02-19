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
                step={type === "time" ? 600 : undefined}
                value={value}
                onChange={
                    type === "time"
                        ? (e) => {
                              const next = e.target.value;
                              // 10분단위로 선택되도록 설정
                              if (type === "time" && /^\d{2}:\d{2}$/.test(next)) {
                                  const [hh, mm] = next.split(":").map(Number);
                                  const rounded = Math.round(mm / 10) * 10;
                                  const clamped = Math.min(50, Math.max(0, rounded));
                                  const normalized = `${String(hh).padStart(2, "0")}:${String(clamped).padStart(2, "0")}`;
                                  onChange(normalized);
                                  return;
                              }

                              onChange(next);
                          }
                        : (e) => onChange(e.target.value)
                }
                min={min}
                max={max}
                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                aria-label={placeholder}
            />
        </div>
    );
}
