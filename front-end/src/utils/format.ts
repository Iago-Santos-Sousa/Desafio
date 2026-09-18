export const formatCurrency = (value: number): string => {
  return `R$ ${value.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`;
};

export const formatDateTime = (value: string): string => {
  return new Date(value).toLocaleString("pt-BR");
};

export const formatBytes = (value: number): string => {
  if (value < 1024) return `${value} B`;
  const units = ["KB", "MB", "GB", "TB"];
  let size = value;
  let index = -1;

  do {
    size /= 1024;
    index += 1;
  } while (size >= 1024 && index < units.length - 1);

  return `${size.toLocaleString("pt-BR", { maximumFractionDigits: 1 })} ${units[index]}`;
};
