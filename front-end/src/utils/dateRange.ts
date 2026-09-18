export interface DashboardDateRange {
  from: string;
  to: string;
}

const BUSINESS_TIME_ZONE = "America/Sao_Paulo";

export const currentBusinessDate = (): string => {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: BUSINESS_TIME_ZONE,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(new Date());

  const values = Object.fromEntries(
    parts.map((part) => [part.type, part.value]),
  );

  return `${values.year}-${values.month}-${values.day}`;
};

export const defaultDashboardDateRange = (): DashboardDateRange => {
  const to = currentBusinessDate();
  return { from: `${to.slice(0, 7)}-01`, to };
};

export const isValidDateRange = (from: string, to: string): boolean => {
  return Boolean(from && to && from <= to);
};
