import type { Page } from "../types/pagination";
type PaginationControllerProps<T> = {
  pageData: Page<T>;
  onPageChange: (pageNumber: number) => void;
};
export const PaginationController = <T,>({
  pageData,
  onPageChange,
}: PaginationControllerProps<T>) => {
  const { number, totalPages, last } = pageData;
  if (totalPages <= 1) return null;

  const pages = [...Array(totalPages).keys()];

  return (
    <div className="flex items-center justify-between px-4 py-3 bg-white border-t border-gray-200 sm:px-6 mt-6">
      <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
        <div>
          <p className="text-sm text-gray-700">
            Affichage de la page{" "}
            <span className="font-semibold">{number + 1}</span> sur{" "}
            <span className="font-semibold">{totalPages}</span>
          </p>
        </div>

        <div>
          <nav
            className="inline-flex -space-x-px rounded-md shadow-sm bg-white"
            aria-label="Pagination"
          >
            <button
              onClick={() => onPageChange(number - 1)}
              disabled={number === 0}
              className="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 disabled:opacity-50 transition-colors"
            >
              <span className="sr-only">Précédent</span>
              <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z"
                  clipRule="evenodd"
                />
              </svg>
            </button>

            {pages.map((p) => (
              <button
                key={p}
                onClick={() => onPageChange(p)}
                className={`relative inline-flex items-center px-4 py-2 text-sm font-semibold ring-1 ring-inset ring-gray-300 transition-all ${
                  p === number
                    ? "z-10 bg-blue-600 text-white focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-blue-600"
                    : "text-gray-900 hover:bg-gray-50"
                }`}
              >
                {p + 1}
              </button>
            ))}

            <button
              onClick={() => onPageChange(number + 1)}
              disabled={last}
              className="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 disabled:opacity-50 transition-colors"
            >
              <span className="sr-only">Suivant</span>
              <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z"
                  clipRule="evenodd"
                />
              </svg>
            </button>
          </nav>
        </div>
      </div>
    </div>
  );
};
