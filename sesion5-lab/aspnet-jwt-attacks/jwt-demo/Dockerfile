FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY JwtDemo.csproj .
RUN dotnet restore
COPY . .
RUN dotnet publish -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:8.0
WORKDIR /app
COPY --from=build /app/publish .
EXPOSE 8202
ENV ASPNETCORE_URLS=http://+:8202
ENTRYPOINT ["dotnet", "JwtDemo.dll"]
