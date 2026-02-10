interface StudentCouncilFeeVerificationInterface {
    id: string;
    verify: boolean;
    s3Link: string;
    requestAt: string;
    responseAt: string;
    denyReason: string;
}
