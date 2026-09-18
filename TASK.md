# Sistema de Ingestão e Análise de Dados em Larga Escala

# O Cenário

A empresa precisa de um sistema capaz de receber, processar e exibir dados de um arquivo pesado de transações financeiras (ou telemetria) contendo milhões de registros, sem travar o navegador do usuário e sem derrubar o servidor por falta de memória (OOM).

## Parte 1: Back-end (Obrigatório ser em Java com Spring Boot + TypeORM(JPA/Hybernate de preferência) com banco de dados PostgreSQL):

O back-end é o coração do desafio, pois lidará com o gargalo de performance.

1. Ingestão de Dados: Um endpoint para upload de um arquivo CSV grande (forneça um script gerador ou um link para um dataset de teste de 1M+ linhas). O processamento deve ser assíncrono (a API retorna sucesso imediatamente enquanto processa em background).
2. Performance e Memória: O arquivo não pode ser carregado inteiramente na memória (RAM). Deve ser demonstrado técnicas de leitura em stream e batch inserts no banco de dados.
3. Endpoints de Consumo:
   - Status do processamento (quantas linhas foram processadas, erros, etc.).
   - Paginação eficiente para listagem dos registros.
   - Endpoint de agregação (ex: soma total de valores por categoria/mês) otimizado.

4. Banco de Dados: Uso de PostgreSQL via Docker. Espera-se a criação de índices adequados para garantir que as queries de agregação e busca sejam performáticas em uma tabela com milhões de linhas.

## Parte 2: Front-end (Obrigatório ser em React + Vite):

A interface deve ser capaz de lidar com a volumetria de dados entregue pelo back-end sem comprometer a experiência do usuário.

1. Upload e Feedback: Uma tela de upload do arquivo que consuma o status do processamento em tempo real (pode ser via Polling, Server-Sent Events (SSE) ou
   WebSockets).
2. Dashboard de Alta Performance:
   - Exibição de métricas agregadas (cards com totais, gráficos simples).
   - Listagem dos dados com paginação server-side ou técnica de Virtualization/Infinite Scroll para evitar sobrecarga no DOM.
   - Gerenciamento de Estado e Estilização: Tecnologias livres (Zustand, Redux, Context API, etc.) e bibliotecas de UI a seu critério.

## Parte 3: Arquitetura e Execução Local:

O projeto deve ser "plug and play" e rodar integralmente via containers.

1. Docker Compose: Um arquivo docker-compose.yml que orquestra o Front-end, Back-end e o Banco de Dados (PostgreSQL).
2. Diferencial (Opcional): Uso de mensageria (ex: RabbitMQ, Kafka) ou ferramentas de cache (Redis) no compose para gerenciar a fila de processamento assíncrono.
3. Documentação (README.md): Explicar como rodar o projeto e focar nas decisões arquiteturais, sendo necessário explicar:
   - Como evitou o estouro de memória no processamento do arquivo.
   - Qual estratégia usou para inserção rápida no banco (batch processing).
   - Como os índices do banco foram pensados para otimizar a leitura.

## Observações:

1. O projeto deve estar containerizado com Docker, garantindo que o ambiente seja iniciado sem erros. A equipe de avaliação não instalará dependências locais, apenas rodará o docker compose up. Certifique-se de que variáveis de ambiente e volumes do banco estejam devidamente mapeados e documentados.

2. O que será avaliado com mais rigor: Gestão de memória no back-end, tempo de resposta das APIs com carga de dados alta, estratégia de indexação no banco relacional e renderização limpa no React.

## Preferências:

1. Parte 1: Back-end (Obrigatório ser em Java com Spring Boot + ORM(JPA/Hybernate de preferência) com banco de dados PostgreSQL):
   - Qual seria a melhor abordagem a usar aqui? Filas com RabbitMQ, java multithreading, Wesockets, Streaming, Polling ou Server-Sent Events (SSE)?
   - Os endpoints devem ter documentação usando swagger.
   - Utilize padrões de projeto/desing patterns na construção do código como, injeção e inversão de dependência, SOLID(de preferência os conceitos S e O), Arquitetura MVC, Repository Pattern, Decorator Pattern, Mapper e outros que for necessário.
   - Crie Migrations do banco de dados com o ORM utilizado.
   - Seguir o padrão Rest ou Restful.
   - Script com Seed inicial de dados se for preciso.

2. Parte 2: Front-end (Obrigatório ser em React + Vite + TypeScript):
   - Instale e utilize bibliotecas como o Axios, Tan Stack Query para fazer requisições junto com Axios, Tailwind CSS para estilização, Biblioteca de componentes MUI e Chart JS para criar os gráficos.
   - Utilize de preferência o Context API do próprio React para gerenciamento de estado.
   - Estilize um design system para a UI/UX com cores claras e vivas.
   - Veja a melhor forma de carregar os dados vindo do back-end em conjunto com a regra escolhida nele, se foi Filas com RabbitMQ, java multithreading, Wesockets, Streaming ou Polling ou Server-Sent Events (SSE).
   - Utilize o padrão composition pattern na criação dos componentes.
   - Crie componentes reutilizáveis.
3. Parte 3: Arquitetura e Execução Local:
   - Dockerfile para o front-end e back-end utilizando técnica de multi stage building.
   - docker-compose.yml orquestrando um container para serviço, ou seja, front-end, back-end e banco de dados, e se for utilizar RabbitMQ também.
   - Utilizar variáveis de ambiente .env tanto no front-end(se precisar) quanto no back-end para subir o docker-compose.yml, evitando expor credenciais no arquivo docker-compose.yml.
