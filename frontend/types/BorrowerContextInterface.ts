import BorrowerInfoInterface from "./BorrowerInfoInterface";

export default interface BorrowerContextInterface {
    borrowerInfo: BorrowerInfoInterface | null;
    setBorrowerInfo: React.Dispatch<React.SetStateAction<BorrowerInfoInterface | null>> | null;
}
