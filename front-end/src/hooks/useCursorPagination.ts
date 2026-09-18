import { useState } from "react";

type Cursor = string | number;

export function useCursorPagination<TCursor extends Cursor>() {
  const [cursor, setCursor] = useState<TCursor>();
  const [history, setHistory] = useState<Array<TCursor | undefined>>([]);

  const next = (nextCursor: TCursor | null | undefined) => {
    if (nextCursor == null) return;

    setHistory((previous) => [...previous, cursor]);
    setCursor(nextCursor);
  };

  const previous = () => {
    if (!history.length) return;

    setCursor(history[history.length - 1]);
    setHistory(history.slice(0, -1));
  };

  const reset = () => {
    setCursor(undefined);
    setHistory([]);
  };

  return {
    cursor,
    hasPrevious: history.length > 0,
    next,
    previous,
    reset,
  };
}
