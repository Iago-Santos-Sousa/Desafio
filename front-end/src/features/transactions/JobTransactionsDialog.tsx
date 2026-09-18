import {
  Autocomplete,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogContent,
  DialogTitle,
  IconButton,
  LinearProgress,
  Paper,
  Skeleton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { useJobCategoriesQuery } from "../../hooks/api/useIngestionQueries";
import { useTransactionsQuery } from "../../hooks/api/useDashboardQueries";
import { formatCurrency, formatDateTime } from "../../utils/format";

export function JobTransactionsDialog({
  jobId,
  open,
  onClose,
}: {
  jobId: string;
  open: boolean;
  onClose: () => void;
}) {
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState<string | null>(null);
  const [cursor, setCursor] = useState<number>();
  const [history, setHistory] = useState<number[]>([]);
  const categories = useJobCategoriesQuery(jobId, search, open);

  const transactions = useTransactionsQuery(
    cursor,
    category ?? "",
    jobId,
    25,
    open,
  );

  const options = categories.data?.pages.flatMap((page) => page.items) ?? [];

  const close = () => {
    setSearch("");
    setCategory(null);
    setCursor(undefined);
    setHistory([]);
    onClose();
  };

  const resetTransactions = (value: string | null) => {
    setCategory(value);
    setCursor(undefined);
    setHistory([]);
  };

  const next = () => {
    if (transactions.data?.nextCursor) {
      setHistory((old) => [...old, cursor ?? 0]);
      setCursor(transactions.data.nextCursor);
    }
  };

  const previous = () => {
    const old = [...history];
    const value = old.pop();
    setHistory(old);
    setCursor(value || undefined);
  };

  return (
    <Dialog
      open={open}
      onClose={close}
      fullWidth
      maxWidth="lg"
      aria-labelledby="job-transactions-title"
    >
      <DialogTitle id="job-transactions-title">
        Transações do job
        <IconButton
          aria-label="Fechar"
          onClick={close}
          sx={{ position: "absolute", right: 8, top: 8 }}
        >
          ×
        </IconButton>
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2}>
          <Autocomplete
            options={options}
            value={category}
            inputValue={search}
            loading={categories.isLoading || categories.isFetchingNextPage}
            filterOptions={(values) => values}
            onInputChange={(_, value) => {
              setSearch(value);
              resetTransactions(null);
            }}
            onChange={(_, value) => resetTransactions(value)}
            ListboxProps={{
              onScroll: (event) => {
                const list = event.currentTarget;
                if (
                  list.scrollTop + list.clientHeight >= list.scrollHeight - 8 &&
                  categories.hasNextPage &&
                  !categories.isFetchingNextPage
                )
                  void categories.fetchNextPage();
              },
            }}
            renderInput={(params) => (
              <TextField
                {...params}
                label="Filtrar categoria"
                placeholder="Digite para buscar"
                InputProps={{
                  ...params.InputProps,
                  endAdornment: (
                    <>
                      {categories.isFetchingNextPage ? (
                        <CircularProgress size={18} />
                      ) : null}
                      {params.InputProps.endAdornment}
                    </>
                  ),
                }}
              />
            )}
          />
          {transactions.isFetching && !transactions.isPending && (
            <LinearProgress />
          )}
          {transactions.isError && (
            <Typography color="error">
              Não foi possível carregar transações.
            </Typography>
          )}
          <Paper variant="outlined">
            <Box sx={{ overflowX: "auto" }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Data</TableCell>
                    <TableCell>Categoria</TableCell>
                    <TableCell align="right">Valor</TableCell>
                    <TableCell>Descrição</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {transactions.isPending
                    ? Array.from({ length: 5 }, (_, index) => (
                        <TableRow key={index}>
                          {Array.from({ length: 5 }, (_, cell) => (
                            <TableCell key={cell}>
                              <Skeleton />
                            </TableCell>
                          ))}
                        </TableRow>
                      ))
                    : transactions.data?.items.map((row) => (
                        <TableRow key={row.id}>
                          <TableCell>{row.id}</TableCell>
                          <TableCell>
                            {formatDateTime(row.occurredAt)}
                          </TableCell>
                          <TableCell>{row.category}</TableCell>
                          <TableCell align="right">
                            {formatCurrency(row.amount)}
                          </TableCell>
                          <TableCell>{row.description}</TableCell>
                        </TableRow>
                      ))}
                </TableBody>
              </Table>
            </Box>
          </Paper>
          {!transactions.isLoading && !transactions.data?.items.length && (
            <Typography color="text.secondary">
              Nenhuma transação encontrada.
            </Typography>
          )}
          <Stack direction="row" justifyContent="flex-end" spacing={1}>
            <Button onClick={previous} disabled={!history.length}>
              Anterior
            </Button>
            <Button
              variant="outlined"
              onClick={next}
              disabled={!transactions.data?.nextCursor}
            >
              Próxima
            </Button>
          </Stack>
        </Stack>
      </DialogContent>
    </Dialog>
  );
}
