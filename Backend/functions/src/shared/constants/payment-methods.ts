/**
 * Payment Methods
 */
export enum PaymentMethod {
  /** Cash on delivery */
  COD = 'cod',

  /** ZaloPay e-wallet */
  ZALOPAY = 'zalopay',

  /** MoMo e-wallet */
  MOMO = 'momo',

  /** SePay bank transfer */
  SEPAY = 'sepay',

  /** Wallet balance */
  WALLET = 'wallet',
}

/**
 * Payment method display names
 */
export const PaymentMethodNames: Record<PaymentMethod, string> = {
  [PaymentMethod.COD]: 'Tiền mặt',
  [PaymentMethod.ZALOPAY]: 'ZaloPay',
  [PaymentMethod.MOMO]: 'MoMo',
  [PaymentMethod.SEPAY]: 'Chuyển khoản ngân hàng',
  [PaymentMethod.WALLET]: 'Ví KTX',
};
