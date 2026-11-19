// validators.js
const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

export const isStrongPassword = (p) => PASSWORD_REGEX.test(p);
export const passwordsMatch = (a, b) => a === b;

