export interface Page<T> {
  content: T[]; // Les données réelles (ex: Building[])
  totalElements: number; // Nombre total d'éléments en BDD
  totalPages: number; // Nombre total de pages
  size: number; // Nombre d'éléments demandés par page
  number: number; // Numéro de la page actuelle (0-indexed)
  numberOfElements: number; // Nombre d'éléments dans la page actuelle
  first: boolean;
  last: boolean;
  empty: boolean;
  pageable: {
    pageNumber: number;
    pageSize: number;
    offset: number;
    paged: boolean;
    unpaged: boolean;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
  };
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
}
