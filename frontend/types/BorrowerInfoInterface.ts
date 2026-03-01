export default interface BorrowerInfoInterface {
    id: string;
    name: string;
    phoneNumber: string;
    department: string;
    accountNumber: string;
    ban: boolean;
    banReason: string | null;
    verify: boolean;
    s3Link: string;
    agreementVersion: string;
}
