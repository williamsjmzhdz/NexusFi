import { useEffect, useState } from 'react';
import { PlusCircle } from 'lucide-react';
import { getIncomes, recordIncome } from '../services/financeService';
import type { IncomeRecord } from '../types/finance';

function fmt(amount: number): string {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}

function fmtDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

const today = new Date().toISOString().split('T')[0];

function IncomePage() {
  // ── Form state ──────────────────────────────────────────────
  const [amount, setAmount] = useState('');
  const [source, setSource] = useState('');
  const [description, setDescription] = useState('');
  const [incomeDate, setIncomeDate] = useState(today);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  // ── List state ───────────────────────────────────────────────
  const [incomes, setIncomes] = useState<IncomeRecord[]>([]);
  const [loadingList, setLoadingList] = useState(true);
  const [listError, setListError] = useState('');

  useEffect(() => {
    fetchIncomes();
  }, []);

  async function fetchIncomes() {
    setLoadingList(true);
    try {
      const data = await getIncomes();
      const sorted = [...data].sort(
        (a, b) => new Date(b.incomeDate).getTime() - new Date(a.incomeDate).getTime()
      );
      setIncomes(sorted);
    } catch {
      setListError('Could not load income records.');
    } finally {
      setLoadingList(false);
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setFormError('');
    setSubmitting(true);
    try {
      await recordIncome({
        amount: parseFloat(amount),
        source: source.trim(),
        description: description.trim() || undefined,
        incomeDate,
      });
      // Reset form
      setAmount('');
      setSource('');
      setDescription('');
      setIncomeDate(today);
      // Refresh list
      await fetchIncomes();
    } catch {
      setFormError('Failed to record income. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900 mb-1">Income</h1>
      <p className="text-sm text-slate-500">Record earnings and view your income history</p>

      {/* ── Add Income Form ───────────────────────────────────── */}
      <div className="mt-6 bg-white rounded-xl border border-slate-100 shadow-sm p-6">
        <h2 className="text-sm font-semibold text-slate-700 mb-4">Record New Income</h2>

        {formError && (
          <div className="mb-4 px-4 py-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-600 text-sm">
            {formError}
          </div>
        )}

        <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {/* Amount */}
          <div>
            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
              Amount <span className="text-rose-500">*</span>
            </label>
            <div className="relative">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm">$</span>
              <input
                type="number"
                min="0.01"
                step="0.01"
                placeholder="0.00"
                value={amount}
                onChange={e => setAmount(e.target.value)}
                required
                className="w-full pl-7 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent focus:bg-white transition"
              />
            </div>
          </div>

          {/* Source */}
          <div>
            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
              Source <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              placeholder="e.g. Salary, Freelance"
              value={source}
              onChange={e => setSource(e.target.value)}
              required
              className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent focus:bg-white transition"
            />
          </div>

          {/* Date */}
          <div>
            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
              Date <span className="text-rose-500">*</span>
            </label>
            <input
              type="date"
              value={incomeDate}
              onChange={e => setIncomeDate(e.target.value)}
              required
              className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent focus:bg-white transition"
            />
          </div>

          {/* Description */}
          <div>
            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
              Description <span className="text-slate-300">(optional)</span>
            </label>
            <input
              type="text"
              placeholder="Optional note"
              value={description}
              onChange={e => setDescription(e.target.value)}
              className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent focus:bg-white transition"
            />
          </div>

          {/* Submit */}
          <div className="sm:col-span-2 flex justify-end">
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-60 text-white text-sm font-medium rounded-xl transition"
            >
              <PlusCircle size={16} />
              {submitting ? 'Recording…' : 'Record Income'}
            </button>
          </div>
        </form>
      </div>

      {/* ── Income History ────────────────────────────────────── */}
      <div className="mt-6">
        <h2 className="text-base font-semibold text-slate-800 mb-3">Income History</h2>
        <div className="bg-white rounded-xl border border-slate-100 shadow-sm overflow-hidden">
          {loadingList ? (
            <div className="divide-y divide-slate-50">
              {[...Array(4)].map((_, i) => (
                <div key={i} className="flex items-center gap-4 px-5 py-4">
                  <div className="h-4 w-24 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 w-32 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 flex-1 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 w-24 bg-slate-100 rounded animate-pulse" />
                </div>
              ))}
            </div>
          ) : listError ? (
            <div className="flex items-center justify-center h-24 text-sm text-rose-500">{listError}</div>
          ) : incomes.length === 0 ? (
            <div className="flex items-center justify-center h-32 text-sm text-slate-400">
              No income recorded yet. Use the form above to add your first entry.
            </div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-100 text-left">
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Date</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Source</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Description</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider text-right">Amount</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {incomes.map((income) => (
                  <tr key={income.id} className="hover:bg-slate-50 transition">
                    <td className="px-5 py-3.5 text-slate-500 whitespace-nowrap">{fmtDate(income.incomeDate)}</td>
                    <td className="px-5 py-3.5 text-slate-700 font-medium">{income.source}</td>
                    <td className="px-5 py-3.5 text-slate-500 max-w-xs truncate">{income.description ?? '—'}</td>
                    <td className="px-5 py-3.5 text-right font-semibold tabular-nums text-emerald-600">
                      +{fmt(income.amount)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

export default IncomePage;
