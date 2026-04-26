import { useEffect, useState } from 'react';
import { TrendingUp, TrendingDown, Wallet } from 'lucide-react';
import { getIncomes, getExpenses, getMovements } from '../services/financeService';
import type { Movement, MovementType } from '../types/finance';

function fmt(amount: number): string {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}

interface SummaryCard {
  label: string;
  value: number;
  icon: React.ElementType;
  color: string;       // text color
  bg: string;          // icon bg
  border: string;      // card left border accent
  sub: string;
}

function fmtDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

const TYPE_STYLES: Record<MovementType, { label: string; className: string }> = {
  ASSIGNMENT: { label: 'Income',   className: 'bg-emerald-50 text-emerald-700' },
  EXPENSE:    { label: 'Expense',  className: 'bg-rose-50 text-rose-700' },
  TRANSFER:   { label: 'Transfer', className: 'bg-indigo-50 text-indigo-700' },
  REBALANCE:  { label: 'Rebalance', className: 'bg-slate-100 text-slate-600' },
};

function DashboardPage() {
  const [totalIncome, setTotalIncome] = useState<number | null>(null);
  const [totalExpenses, setTotalExpenses] = useState<number | null>(null);
  const [movements, setMovements] = useState<Movement[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function fetchSummary() {
      try {
        const [incomes, expenses, movs] = await Promise.all([getIncomes(), getExpenses(), getMovements()]);
        const income = incomes.reduce((sum, r) => sum + r.amount, 0);
        const expense = expenses.reduce((sum, r) => sum + r.amount, 0);
        setTotalIncome(income);
        setTotalExpenses(expense);
        const sorted = [...movs].sort(
          (a, b) => new Date(b.movementDate).getTime() - new Date(a.movementDate).getTime()
        );
        setMovements(sorted.slice(0, 10));
      } catch {
        setError('Could not load financial data. Please try again.');
      } finally {
        setLoading(false);
      }
    }
    fetchSummary();
  }, []);

  const net = (totalIncome ?? 0) - (totalExpenses ?? 0);

  const cards: SummaryCard[] = [
    {
      label: 'Net Balance',
      value: net,
      icon: Wallet,
      color: net >= 0 ? 'text-indigo-600' : 'text-rose-600',
      bg: net >= 0 ? 'bg-indigo-50' : 'bg-rose-50',
      border: net >= 0 ? 'border-l-indigo-500' : 'border-l-rose-500',
      sub: 'Income minus expenses',
    },
    {
      label: 'Total Income',
      value: totalIncome ?? 0,
      icon: TrendingUp,
      color: 'text-emerald-600',
      bg: 'bg-emerald-50',
      border: 'border-l-emerald-500',
      sub: 'All time earnings',
    },
    {
      label: 'Total Expenses',
      value: totalExpenses ?? 0,
      icon: TrendingDown,
      color: 'text-rose-600',
      bg: 'bg-rose-50',
      border: 'border-l-rose-500',
      sub: 'All time spending',
    },
  ];

  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900 mb-1">Dashboard</h1>
      <p className="text-sm text-slate-500">Your financial overview</p>

      {error && (
        <div className="mt-6 px-4 py-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-600 text-sm">
          {error}
        </div>
      )}

      <div className="mt-6 grid grid-cols-1 sm:grid-cols-3 gap-4">
        {cards.map((card) => {
          const Icon = card.icon;
          return (
            <div
              key={card.label}
              className={`bg-white rounded-xl border border-slate-100 border-l-4 ${card.border} shadow-sm p-5`}
            >
              <div className="flex items-center justify-between mb-4">
                <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  {card.label}
                </span>
                <span className={`p-2 rounded-lg ${card.bg}`}>
                  <Icon size={16} className={card.color} />
                </span>
              </div>

              {loading ? (
                <div className="h-8 w-32 bg-slate-100 rounded-lg animate-pulse" />
              ) : (
                <p className={`text-2xl font-bold ${card.color}`}>{fmt(card.value)}</p>
              )}

              <p className="mt-1 text-xs text-slate-400">{card.sub}</p>
            </div>
          );
        })}
      </div>

      {/* Recent Movements */}
      <div className="mt-8">
        <h2 className="text-base font-semibold text-slate-800 mb-3">Recent Movements</h2>
        <div className="bg-white rounded-xl border border-slate-100 shadow-sm overflow-hidden">
          {loading ? (
            <div className="divide-y divide-slate-50">
              {[...Array(4)].map((_, i) => (
                <div key={i} className="flex items-center gap-4 px-5 py-4">
                  <div className="h-4 w-20 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 w-16 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 flex-1 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 w-24 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 w-20 bg-slate-100 rounded animate-pulse" />
                </div>
              ))}
            </div>
          ) : movements.length === 0 ? (
            <div className="flex items-center justify-center h-32 text-sm text-slate-400">
              No movements yet. Add income or expenses to get started.
            </div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-100 text-left">
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Date</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Type</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Description</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Category</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider text-right">Amount</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {movements.map((m) => {
                  const style = TYPE_STYLES[m.type];
                  const isDebit = m.type === 'EXPENSE';
                  return (
                    <tr key={m.id} className="hover:bg-slate-50 transition">
                      <td className="px-5 py-3.5 text-slate-500 whitespace-nowrap">{fmtDate(m.movementDate)}</td>
                      <td className="px-5 py-3.5">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium ${style.className}`}>
                          {style.label}
                        </span>
                      </td>
                      <td className="px-5 py-3.5 text-slate-700 max-w-xs truncate">{m.description ?? '—'}</td>
                      <td className="px-5 py-3.5 text-slate-500">{m.categoryName}</td>
                      <td className={`px-5 py-3.5 text-right font-medium tabular-nums ${isDebit ? 'text-rose-600' : 'text-emerald-600'}`}>
                        {isDebit ? '−' : '+'}{fmt(m.amount)}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

export default DashboardPage;

