import BorrowerInfoInterface from "./BorrowerInfoInterface";

export default interface SearchedBorrowersContextInteface {
    searchedBorrowers: BorrowerInfoInterface[];
    setSearchedBorrowers: React.Dispatch<React.SetStateAction<BorrowerInfoInterface[]>> | null;
}
