"use client";

import Button from "@/components/utilities/Button";
import Input from "@/components/utilities/Input";
import { useContext, useRef, useState } from "react";
import BorrowerSearchResultCard from "./BorrowerSearchResultCard";
import SearchedBorrowersContext from "@/context/SearchedContext";
import BorrowerInfoInterface from "@/types/BorrowerInfoInterface";
import axios from "axios";
import API_SERVER from "@/apiServer";
import errorHandler from "@/utilities/errorHandler";

const onlyDigits = /^[0-9]+$/;
const onlyKorean = /^[가-힣]+$/;

export default function BorrowerSearchPage() {
    const { searchedBorrowers, setSearchedBorrowers } = useContext(SearchedBorrowersContext);
    const [result, setResult] = useState<BorrowerInfoInterface[]>([]);
    const searchKeywordRef = useRef<HTMLInputElement>(null);

    const cacheSearch = (keyword: string, type: String) => {
        return searchedBorrowers.filter((sb) => {
            if (type == "ID" && sb.id.includes(keyword)) {
                // 학번(아이디)
                return sb;
            } else {
                // 이름
                if (sb.name.includes(keyword)) {
                    return sb;
                }
            }
        });
    };

    const search = async () => {
        if (!(searchKeywordRef.current instanceof HTMLInputElement)) {
            alert("새로고침 후 다시 시도해주세요.");
            return;
        }

        if (setSearchedBorrowers == null) {
            alert("새로고침 후 다시 시도해주세요.");
            return;
        }

        try {
            // 검색해오기
            // 숫자만 있다 -> 학번
            // 한글만 있다 -> 이름
            // 1차 캐시 뒤지기 -> 2차 요청보내기

            const keyword = searchKeywordRef.current.value;
            const searchType = onlyDigits.test(keyword) ? "ID" : onlyKorean.test(keyword) ? "NAME" : "";

            if (searchType == "") {
                alert("한글또는 숫자만 입력해주세요.");
                return;
            }
            const cacheSearchResult = cacheSearch(keyword, searchType);
            if (cacheSearchResult.length == 0) {
                const res = await axios.get(
                    `${API_SERVER}/borrowers/search?searchType=${searchType}&keyword=${keyword}`,
                    { withCredentials: true },
                );
                const result: BorrowerInfoInterface[] = res.data.data;
                setResult(result);

                const removeSame = result.filter(({ id }) => searchedBorrowers.find((sb) => sb.id == id) == undefined);
                setSearchedBorrowers((prev) => removeSame.concat(prev));
            } else {
                setResult(cacheSearchResult);
            }
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert("알 수 없는 에러입니다. 잠시후 다시 시도해보시고, 지속적으로 발생하는 경우 개발자에게 연락해주세요.");
            return;
        }
    };

    return (
        <div>
            <div>
                <p className="black-20px mt-5">🔎 대여자 검색</p>
                <p>카드를 누르면 각 개인별 세부정보를 볼 수 있습니다</p>
            </div>
            <div className="mt-2">
                <Input
                    placeholder="학번이나 이름을 검색해주세요"
                    ref={searchKeywordRef}
                    onKeyDown={(e) => {
                        if (e.key == "Enter") {
                            search();
                        }
                    }}
                />
            </div>
            <div className="mt-2 flex flex-col gap-1 pb-4">
                {result.map((b) => (
                    <BorrowerSearchResultCard
                        id={b.id}
                        ban={b.ban}
                        department={b.department}
                        name={b.name}
                        key={b.id}
                    />
                ))}
            </div>
            <div className="fixed max-w-125 bottom-0 left-1/2 translate-x-[-50%] w-full px-5 pb-4 bg-back">
                <Button title="검색" className="w-full py-3 bold-18px" onClick={search} />
            </div>
        </div>
    );
}
