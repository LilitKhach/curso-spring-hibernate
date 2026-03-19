// API client configuration
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export interface Socia {
  id: number;
  nombre: string;
  apellido: string;
}

export interface DashboardSummary {
  totalSocias: number;
  totalBeneficio: number;
  averageBeneficio: number;
}

export interface ContratoBeneficio {
  contratoId: number;
  contratoNumero: string;
  beneficio: number;
}

export interface SociaBeneficio {
  sociaId: number;
  nombre: string;
  apellido: string;
  beneficioTotal: number;
  contratos: ContratoBeneficio[];
}

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string = API_BASE_URL) {
    this.baseUrl = baseUrl;
  }

  // Socia endpoints
  async getSocias(): Promise<Socia[]> {
    return this.get('/socias');
  }

  async getSociaById(id: number): Promise<Socia> {
    return this.get(`/socias/${id}`);
  }

  async createSocia(socia: Socia): Promise<Socia> {
    return this.post('/socias', socia);
  }

  async updateSocia(id: number, socia: Socia): Promise<Socia> {
    return this.put(`/socias/${id}`, socia);
  }

  async deleteSocia(id: number): Promise<void> {
    return this.delete(`/socias/${id}`);
  }

  // Dashboard endpoints
  async getDashboard(from?: string, to?: string, minBeneficio?: number): Promise<SociaBeneficio[]> {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);
    if (minBeneficio) params.append('minBeneficio', minBeneficio.toString());

    return this.get(`/socias/dashboard${params.toString() ? '?' + params.toString() : ''}`);
  }

  async getDashboardSummary(from?: string, to?: string, minBeneficio?: number): Promise<DashboardSummary> {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);
    if (minBeneficio) params.append('minBeneficio', minBeneficio.toString());

    return this.get(`/socias/dashboard/summary${params.toString() ? '?' + params.toString() : ''}`);
  }

  async getSociaBeneficio(id: number, from?: string, to?: string): Promise<number> {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);

    return this.get(`/socias/${id}/beneficio${params.toString() ? '?' + params.toString() : ''}`);
  }

  // Contrato endpoints
  async getContratosBySocia(sociaId: number, from?: string, to?: string): Promise<any[]> {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);

    return this.get(`/contratos/socia/${sociaId}${params.toString() ? '?' + params.toString() : ''}`);
  }

  async getContratoDetail(sociaId: number, contratoId: number, from?: string, to?: string): Promise<any> {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);

    return this.get(`/contratos/socia/${sociaId}/contrato/${contratoId}${params.toString() ? '?' + params.toString() : ''}`);
  }

  // Transaccion endpoints
  async getTransacionesByContrato(contratoId: number, from?: string, to?: string): Promise<any[]> {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);

    return this.get(`/transacciones/contrato/${contratoId}${params.toString() ? '?' + params.toString() : ''}`);
  }

  // HTTP helper methods
  private async get(endpoint: string): Promise<any> {
    return this.request(endpoint, 'GET');
  }

  private async post(endpoint: string, body: any): Promise<any> {
    return this.request(endpoint, 'POST', body);
  }

  private async put(endpoint: string, body: any): Promise<any> {
    return this.request(endpoint, 'PUT', body);
  }

  private async delete(endpoint: string): Promise<any> {
    return this.request(endpoint, 'DELETE');
  }

  private async request(endpoint: string, method: string, body?: any): Promise<any> {
    const options: RequestInit = {
      method,
      headers: {
        'Content-Type': 'application/json',
      },
    };

    if (body) {
      options.body = JSON.stringify(body);
    }

    try {
      const response = await fetch(`${this.baseUrl}${endpoint}`, options);

      if (!response.ok) {
        throw new Error(`API error: ${response.status} ${response.statusText}`);
      }

      if (response.status === 204) {
        return null;
      }

      return response.json();
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }
}

export default new ApiClient();

